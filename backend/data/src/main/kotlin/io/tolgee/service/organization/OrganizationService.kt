package io.tolgee.service.organization

import io.tolgee.configuration.tolgee.TolgeeProperties
import io.tolgee.constants.Message
import io.tolgee.dtos.request.organization.OrganizationDto
import io.tolgee.dtos.request.organization.OrganizationRequestParamsDto
import io.tolgee.dtos.request.validators.exceptions.ValidationException
import io.tolgee.exceptions.NotFoundException
import io.tolgee.model.Organization
import io.tolgee.model.Permission
import io.tolgee.model.UserAccount
import io.tolgee.model.enums.OrganizationRoleType
import io.tolgee.model.enums.ProjectPermissionType
import io.tolgee.model.views.OrganizationView
import io.tolgee.repository.OrganizationRepository
import io.tolgee.security.AuthenticationFacade
import io.tolgee.service.AvatarService
import io.tolgee.service.InvitationService
import io.tolgee.service.project.ProjectService
import io.tolgee.service.security.PermissionService
import io.tolgee.service.security.UserPreferencesService
import io.tolgee.util.SlugGenerator
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.io.InputStream

@Service
@Transactional
class OrganizationService(
  private val organizationRepository: OrganizationRepository,
  private val authenticationFacade: AuthenticationFacade,
  private val slugGenerator: SlugGenerator,
  private val organizationRoleService: OrganizationRoleService,
  private val invitationService: InvitationService,
  private val avatarService: AvatarService,
  @Lazy
  private val userPreferencesService: UserPreferencesService,
  private val tolgeeProperties: TolgeeProperties,
  private val permissionService: PermissionService
) {

  @set:Autowired
  lateinit var projectService: ProjectService

  @Transactional
  fun create(
    createDto: OrganizationDto,
  ): Organization {
    return create(createDto, authenticationFacade.userAccountEntity)
  }

  @Transactional
  fun create(
    createDto: OrganizationDto,
    userAccount: UserAccount
  ): Organization {
    if (createDto.slug != null && !validateSlugUniqueness(createDto.slug!!)) {
      throw ValidationException(Message.ADDRESS_PART_NOT_UNIQUE)
    }

    val slug = createDto.slug
      ?: generateSlug(createDto.name)

    val basePermission = Permission(
      type = ProjectPermissionType.VIEW,
    )

    val organization = Organization(
      name = createDto.name,
      description = createDto.description,
      slug = slug,
    )

    organization.basePermission = basePermission

    basePermission.organization = organization
    permissionService.create(basePermission)

    organizationRepository.save(organization)
    organizationRoleService.grantOwnerRoleToUser(userAccount, organization)
    return organization
  }

  fun createPreferred(userAccount: UserAccount, name: String = userAccount.name): Organization {
    val safeName = if (name.isNotEmpty() || name.length >= 3)
      name
    else
      "${userAccount.username.take(3)} Organization"
    return this.create(OrganizationDto(name = safeName), userAccount = userAccount)
  }

  private fun generateSlug(name: String) =
    slugGenerator.generate(name, 3, 60) {
      this.validateSlugUniqueness(it)
    }

  /**
   * Returns any organizations accessible by user.
   */
  fun findPreferred(userAccountId: Long, exceptOrganizationId: Long = 0): Organization? {
    return organizationRepository.findPreferred(
      userId = userAccountId,
      exceptOrganizationId,
      PageRequest.of(0, 1)
    ).content.firstOrNull()
  }

  /**
   * Returns existing or created organization which seems to be potentially preferred.
   */
  fun findOrCreatePreferred(userAccount: UserAccount, exceptOrganizationId: Long = 0): Organization? {
    return findPreferred(userAccount.id, exceptOrganizationId) ?: let {
      if (tolgeeProperties.authentication.userCanCreateOrganizations || userAccount.role == UserAccount.Role.ADMIN) {
        return@let createPreferred(userAccount)
      }
      null
    }
  }

  fun findPermittedPaged(
    pageable: Pageable,
    requestParamsDto: OrganizationRequestParamsDto,
    exceptOrganizationId: Long? = null
  ): Page<OrganizationView> {
    return findPermittedPaged(
      pageable,
      requestParamsDto.filterCurrentUserOwner,
      requestParamsDto.search,
      exceptOrganizationId
    )
  }

  fun findPermittedPaged(
    pageable: Pageable,
    filterCurrentUserOwner: Boolean = false,
    search: String? = null,
    exceptOrganizationId: Long? = null
  ): Page<OrganizationView> {
    return organizationRepository.findAllPermitted(
      userId = authenticationFacade.userAccount.id,
      pageable = pageable,
      roleType = if (filterCurrentUserOwner) OrganizationRoleType.OWNER else null,
      search = search,
      exceptOrganizationId = exceptOrganizationId
    )
  }

  fun get(id: Long): Organization {
    return organizationRepository.findByIdOrNull(id) ?: throw NotFoundException(Message.ORGANIZATION_NOT_FOUND)
  }

  fun find(id: Long): Organization? {
    return organizationRepository.findByIdOrNull(id)
  }

  fun get(slug: String): Organization {
    return organizationRepository.getOneBySlug(slug) ?: throw NotFoundException(Message.ORGANIZATION_NOT_FOUND)
  }

  fun find(slug: String): Organization? {
    return organizationRepository.getOneBySlug(slug)
  }

  fun edit(id: Long, editDto: OrganizationDto): OrganizationView {
    val organization = this.find(id) ?: throw NotFoundException()

    val newSlug = editDto.slug
    if (newSlug == null) {
      editDto.slug = organization.slug
    }

    if (newSlug != organization.slug && !validateSlugUniqueness(newSlug!!)) {
      throw ValidationException(Message.ADDRESS_PART_NOT_UNIQUE)
    }

    organization.name = editDto.name
    organization.description = editDto.description
    organization.slug = newSlug

    organizationRepository.save(organization)
    return OrganizationView.of(organization, OrganizationRoleType.OWNER)
  }

  @Transactional
  fun delete(id: Long) {
    val organization = this.find(id) ?: throw NotFoundException()

    projectService.findAllInOrganization(id).forEach {
      projectService.deleteProject(it.id)
    }

    invitationService.getForOrganization(organization).forEach { invitation ->
      invitationService.delete(invitation)
    }

    organization.preferredBy
      .toList() // we need to clone it so hibernate doesn't change it concurrently
      .forEach {
        it.preferredOrganization = findOrCreatePreferred(
          userAccount = it.userAccount,
          exceptOrganizationId = organization.id
        )
        userPreferencesService.save(it)
      }

    organizationRoleService.deleteAllInOrganization(organization)

    this.organizationRepository.delete(organization)
    avatarService.unlinkAvatarFiles(organization)
  }

  @Transactional
  fun removeAvatar(organization: Organization) {
    avatarService.removeAvatar(organization)
  }

  @Transactional
  fun setAvatar(organization: Organization, avatar: InputStream) {
    avatarService.setAvatar(organization, avatar)
  }

  /**
   * Checks address part uniqueness
   * @return Returns true if valid
   */
  fun validateSlugUniqueness(slug: String): Boolean {
    return organizationRepository.countAllBySlug(slug) < 1
  }

  fun isThereAnotherOwner(id: Long): Boolean {
    return organizationRoleService.isAnotherOwnerInOrganization(id)
  }

  fun generateSlug(name: String, oldSlug: String? = null): String {
    return slugGenerator.generate(name, 3, 60) {
      if (it == oldSlug) {
        return@generate true
      }
      this.validateSlugUniqueness(it)
    }
  }

  /**
   * Returns all organizations which are owned only by the specified user
   */
  fun getAllSingleOwnedByUser(userAccount: UserAccount) = organizationRepository.getAllSingleOwnedByUser(userAccount)

  fun getOrganizationAndCheckUserIsOwner(organizationId: Long): Organization {
    val organization = this.get(organizationId)
    organizationRoleService.checkUserIsOwner(organization.id)
    return organization
  }

  fun deleteAllByName(name: String) {
    organizationRepository.findAllByName(name).forEach {
      this.delete(it.id)
    }
  }

  fun saveAll(organizations: List<Organization>) {
    organizationRepository.saveAll(organizations)
  }

  fun findAllPaged(pageable: Pageable, search: String?, userId: Long): Page<OrganizationView> {
    return organizationRepository.findAllViews(pageable, search, userId)
  }

  fun getProjectOwner(projectId: Long): Organization {
    return organizationRepository.getProjectOwner(projectId)
  }

  fun setBasePermission(organizationId: Long, permissionType: ProjectPermissionType) {
    val organization = get(organizationId)
    val basePermission = organization.basePermission
    basePermission.type = permissionType
    basePermission.scopes = arrayOf()
    permissionService.save(basePermission)
  }
}
