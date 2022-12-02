/*
 * Copyright (c) 2020. Tolgee
 */

package io.tolgee.api.v2.controllers

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import io.tolgee.activity.RequestActivity
import io.tolgee.activity.data.ActivityType
import io.tolgee.api.v2.hateoas.invitation.ProjectInvitationModel
import io.tolgee.api.v2.hateoas.invitation.ProjectInvitationModelAssembler
import io.tolgee.api.v2.hateoas.key.LanguageConfigItemModelAssembler
import io.tolgee.api.v2.hateoas.machineTranslation.LanguageConfigItemModel
import io.tolgee.api.v2.hateoas.project.ProjectModel
import io.tolgee.api.v2.hateoas.project.ProjectModelAssembler
import io.tolgee.api.v2.hateoas.project.ProjectTransferOptionModel
import io.tolgee.api.v2.hateoas.project.ProjectWithStatsModel
import io.tolgee.api.v2.hateoas.user_account.UserAccountInProjectModel
import io.tolgee.api.v2.hateoas.user_account.UserAccountInProjectModelAssembler
import io.tolgee.constants.Message
import io.tolgee.dtos.misc.CreateProjectInvitationParams
import io.tolgee.dtos.request.AutoTranslationSettingsDto
import io.tolgee.dtos.request.SetMachineTranslationSettingsDto
import io.tolgee.dtos.request.project.CreateProjectDTO
import io.tolgee.dtos.request.project.EditProjectDTO
import io.tolgee.dtos.request.project.ProjectInviteUserDto
import io.tolgee.exceptions.BadRequestException
import io.tolgee.facade.ProjectWithStatsFacade
import io.tolgee.model.Language
import io.tolgee.model.Permission
import io.tolgee.model.Permission.ProjectPermissionType
import io.tolgee.model.views.ProjectWithLanguagesView
import io.tolgee.model.views.UserAccountInProjectWithLanguagesView
import io.tolgee.security.AuthenticationFacade
import io.tolgee.security.NeedsSuperJwtToken
import io.tolgee.security.apiKeyAuth.AccessWithApiKey
import io.tolgee.security.project_auth.AccessWithAnyProjectPermission
import io.tolgee.security.project_auth.AccessWithProjectPermission
import io.tolgee.security.project_auth.ProjectHolder
import io.tolgee.service.ImageUploadService
import io.tolgee.service.InvitationService
import io.tolgee.service.LanguageService
import io.tolgee.service.machineTranslation.MtServiceConfigService
import io.tolgee.service.organization.OrganizationRoleService
import io.tolgee.service.organization.OrganizationService
import io.tolgee.service.project.ProjectService
import io.tolgee.service.security.PermissionService
import io.tolgee.service.security.SecurityService
import io.tolgee.service.security.UserAccountService
import io.tolgee.service.translation.AutoTranslationService
import org.springdoc.api.annotations.ParameterObject
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PagedResourcesAssembler
import org.springframework.hateoas.CollectionModel
import org.springframework.hateoas.MediaTypes
import org.springframework.hateoas.PagedModel
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import javax.validation.Valid

@Suppress(names = ["MVCPathVariableInspection", "SpringJavaInjectionPointsAutowiringInspection"])
@RestController
@CrossOrigin(origins = ["*"])
@RequestMapping(value = ["/v2/projects"])
@Tag(name = "Projects")
class
V2ProjectsController(
  private val projectService: ProjectService,
  private val projectHolder: ProjectHolder,
  private val arrayResourcesAssembler: PagedResourcesAssembler<ProjectWithLanguagesView>,
  private val userArrayResourcesAssembler: PagedResourcesAssembler<UserAccountInProjectWithLanguagesView>,
  private val userAccountInProjectModelAssembler: UserAccountInProjectModelAssembler,
  private val projectModelAssembler: ProjectModelAssembler,
  private val languageConfigItemModelAssembler: LanguageConfigItemModelAssembler,
  private val userAccountService: UserAccountService,
  private val permissionService: PermissionService,
  private val authenticationFacade: AuthenticationFacade,
  private val securityService: SecurityService,
  private val invitationService: InvitationService,
  private val organizationService: OrganizationService,
  private val organizationRoleService: OrganizationRoleService,
  private val imageUploadService: ImageUploadService,
  private val projectInvitationModelAssembler: ProjectInvitationModelAssembler,
  private val mtServiceConfigService: MtServiceConfigService,
  private val autoTranslateService: AutoTranslationService,
  private val languageService: LanguageService,
  private val projectWithStatsFacade: ProjectWithStatsFacade
) {
  @Operation(summary = "Returns all projects where current user has any permission")
  @GetMapping("", produces = [MediaTypes.HAL_JSON_VALUE])
  fun getAll(
    @ParameterObject pageable: Pageable,
    @RequestParam("search") search: String?
  ): PagedModel<ProjectModel> {
    val projects = projectService.findPermittedPaged(pageable, search)
    return arrayResourcesAssembler.toModel(projects, projectModelAssembler)
  }

  @Operation(summary = "Returns all projects (including statistics) where current user has any permission")
  @GetMapping("/with-stats", produces = [MediaTypes.HAL_JSON_VALUE])
  fun getAllWithStatistics(
    @ParameterObject pageable: Pageable,
    @RequestParam("search") search: String?
  ): PagedModel<ProjectWithStatsModel> {
    val projects = projectService.findPermittedPaged(pageable, search)
    return projectWithStatsFacade.getPagedModelWithStats(projects)
  }

  @GetMapping("/{projectId}")
  @AccessWithAnyProjectPermission
  @AccessWithApiKey
  @Operation(summary = "Returns project by id")
  fun get(@PathVariable("projectId") projectId: Long): ProjectModel {
    return projectService.getView(projectId).let {
      projectModelAssembler.toModel(it)
    }
  }

  @GetMapping("/{projectId}/users")
  @Operation(summary = "Returns project all users, who have permission to access project")
  @AccessWithProjectPermission(ProjectPermissionType.MANAGE)
  @NeedsSuperJwtToken
  fun getAllUsers(
    @PathVariable("projectId") projectId: Long,
    @ParameterObject pageable: Pageable,
    @RequestParam("search", required = false) search: String?
  ): PagedModel<UserAccountInProjectModel> {
    return userAccountService.getAllInProjectWithPermittedLanguages(projectId, pageable, search).let { users ->
      userArrayResourcesAssembler.toModel(users, userAccountInProjectModelAssembler)
    }
  }

  @PutMapping("/{projectId:[0-9]+}/avatar", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
  @Operation(summary = "Uploads organizations avatar")
  @ResponseStatus(HttpStatus.OK)
  @AccessWithProjectPermission(ProjectPermissionType.MANAGE)
  fun uploadAvatar(
    @RequestParam("avatar") avatar: MultipartFile,
    @PathVariable projectId: Long
  ): ProjectModel {
    imageUploadService.validateIsImage(avatar)
    projectService.setAvatar(projectHolder.projectEntity, avatar.inputStream)
    return projectModelAssembler.toModel(projectService.getView(projectId))
  }

  @DeleteMapping("/{projectId:[0-9]+}/avatar")
  @Operation(summary = "Deletes organization avatar")
  @ResponseStatus(HttpStatus.OK)
  fun removeAvatar(
    @PathVariable projectId: Long
  ): ProjectModel {
    projectService.removeAvatar(projectHolder.projectEntity)
    return projectModelAssembler.toModel(projectService.getView(projectId))
  }

  @PutMapping("/{projectId}/users/{userId}/set-permissions/{permissionType}")
  @AccessWithProjectPermission(ProjectPermissionType.MANAGE)
  @Operation(summary = "Sets user's direct permission")
  @NeedsSuperJwtToken
  fun setUsersPermissions(
    @PathVariable("projectId") projectId: Long,
    @PathVariable("userId") userId: Long,
    @PathVariable("permissionType") permissionType: ProjectPermissionType,
    @RequestParam languages: MutableSet<Long>?,
  ) {
    checkNotCurrentUser(userId)
    val languageEntities = getLanguagesAndCheckFromProject(languages, projectId)
    permissionService.setUserDirectPermission(
      projectId = projectId,
      userId = userId,
      newPermissionType = permissionType,
      languages = languageEntities.toSet()
    )
  }

  private fun checkNotCurrentUser(userId: Long) {
    if (userId == authenticationFacade.userAccount.id) {
      throw BadRequestException(Message.CANNOT_SET_YOUR_OWN_PERMISSIONS)
    }
  }

  private fun getLanguagesAndCheckFromProject(
    languages: Set<Long>?,
    projectId: Long
  ): List<Language> {
    languages?.let {
      val languageEntities = languageService.findByIdIn(languages)
      languageEntities.forEach {
        if (it.project.id != projectId) {
          throw BadRequestException(Message.LANGUAGE_NOT_FROM_PROJECT)
        }
      }
      return languageEntities
    }
    return listOf()
  }

  @PutMapping("/{projectId}/users/{userId}/revoke-access")
  @AccessWithProjectPermission(ProjectPermissionType.MANAGE)
  @Operation(summary = "Revokes user's access")
  @NeedsSuperJwtToken
  fun revokePermission(
    @PathVariable("projectId") projectId: Long,
    @PathVariable("userId") userId: Long
  ) {
    if (userId == authenticationFacade.userAccount.id) {
      throw BadRequestException(Message.CAN_NOT_REVOKE_OWN_PERMISSIONS)
    }
    permissionService.revoke(projectId, userId)
  }

  @PostMapping(value = [""])
  @Operation(summary = "Creates project with specified languages")
  @RequestActivity(ActivityType.CREATE_PROJECT)
  fun createProject(@RequestBody @Valid dto: CreateProjectDTO): ProjectModel {
    organizationRoleService.checkUserIsOwner(dto.organizationId)
    val project = projectService.createProject(dto)
    return projectModelAssembler.toModel(projectService.getView(project.id))
  }

  @Operation(summary = "Modifies project")
  @PutMapping(value = ["/{projectId}"])
  @AccessWithProjectPermission(ProjectPermissionType.MANAGE)
  @RequestActivity(ActivityType.EDIT_PROJECT)
  @NeedsSuperJwtToken
  fun editProject(@RequestBody @Valid dto: EditProjectDTO): ProjectModel {
    val project = projectService.editProject(projectHolder.project.id, dto)
    return projectModelAssembler.toModel(projectService.getView(project.id))
  }

  @DeleteMapping(value = ["/{projectId}"])
  @Operation(summary = "Deletes project by id")
  @NeedsSuperJwtToken
  fun deleteProject(@PathVariable projectId: Long) {
    securityService.checkProjectPermission(projectId, ProjectPermissionType.MANAGE)
    projectService.deleteProject(projectId)
  }

  @PutMapping(value = ["/{projectId:[0-9]+}/transfer-to-organization/{organizationId:[0-9]+}"])
  @Operation(summary = "Transfers project's ownership to organization")
  @AccessWithProjectPermission(ProjectPermissionType.MANAGE)
  @NeedsSuperJwtToken
  fun transferProjectToOrganization(@PathVariable projectId: Long, @PathVariable organizationId: Long) {
    organizationRoleService.checkUserIsOwner(organizationId)
    projectService.transferToOrganization(projectId, organizationId)
  }

  @PutMapping(value = ["/{projectId:[0-9]+}/leave"])
  @Operation(summary = "Leave project")
  @NeedsSuperJwtToken
  @AccessWithProjectPermission(ProjectPermissionType.VIEW)
  fun leaveProject() {
    permissionService.leave(projectHolder.projectEntity, authenticationFacade.userAccount.id)
  }

  @AccessWithProjectPermission(ProjectPermissionType.MANAGE)
  @Operation(summary = "Returns transfer option")
  @GetMapping(value = ["/{projectId:[0-9]+}/transfer-options"])
  fun getTransferOptions(@RequestParam search: String? = ""): CollectionModel<ProjectTransferOptionModel> {
    val project = projectHolder.project
    val organizations = organizationService.findPermittedPaged(
      PageRequest.of(0, 10),
      true,
      search,
      project.organizationOwnerId
    )
    val options = organizations.content.map {
      ProjectTransferOptionModel(
        name = it.name,
        slug = it.slug,
        id = it.id,
      )
    }.toMutableList()
    options.sortBy { it.name }
    return CollectionModel.of(options)
  }

  @PutMapping("/{projectId}/invite")
  @Operation(summary = "Generates user invitation link for project")
  @AccessWithProjectPermission(ProjectPermissionType.MANAGE)
  @NeedsSuperJwtToken
  fun inviteUser(@RequestBody @Valid invitation: ProjectInviteUserDto): ProjectInvitationModel {
    val languages = getLanguagesAndCheckFromProject(invitation.languages, projectHolder.project.id)
    val params = CreateProjectInvitationParams(
      project = projectHolder.projectEntity,
      type = invitation.type!!,
      languages = languages,
      email = invitation.email,
      name = invitation.name
    )
    return projectInvitationModelAssembler.toModel(invitationService.create(params))
  }

  @GetMapping("{projectId:[0-9]+}/invitations")
  @Operation(summary = "Returns all invitations to project")
  @AccessWithProjectPermission(Permission.ProjectPermissionType.MANAGE)
  @NeedsSuperJwtToken
  fun getProjectInvitations(@PathVariable("projectId") id: Long): CollectionModel<ProjectInvitationModel> {
    val project = projectService.get(id)
    securityService.checkProjectPermission(id, ProjectPermissionType.MANAGE)
    val invitations = invitationService.getForProject(project)
    return projectInvitationModelAssembler.toCollectionModel(invitations)
  }

  @GetMapping("/{projectId}/machine-translation-service-settings")
  @Operation(summary = "Returns machine translation settings for project")
  @AccessWithProjectPermission(ProjectPermissionType.VIEW)
  fun getMachineTranslationSettings(): CollectionModel<LanguageConfigItemModel> {
    val data = mtServiceConfigService.getProjectSettings(projectHolder.projectEntity)
    return languageConfigItemModelAssembler.toCollectionModel(data)
  }

  @PutMapping("/{projectId}/machine-translation-service-settings")
  @AccessWithProjectPermission(ProjectPermissionType.MANAGE)
  @Operation(summary = "Sets machine translation settings for project")
  fun setMachineTranslationSettings(
    @RequestBody dto: SetMachineTranslationSettingsDto
  ): CollectionModel<LanguageConfigItemModel> {
    mtServiceConfigService.setProjectSettings(projectHolder.projectEntity, dto)
    return getMachineTranslationSettings()
  }

  @PutMapping("/{projectId}/auto-translation-settings")
  @AccessWithProjectPermission(ProjectPermissionType.MANAGE)
  @Operation(summary = "Sets auto translation settings for project")
  fun setAutoTranslationSettings(
    @RequestBody dto: AutoTranslationSettingsDto
  ): AutoTranslationSettingsDto {
    autoTranslateService.saveConfig(projectHolder.projectEntity, dto)
    return dto
  }

  @GetMapping("/{projectId}/auto-translation-settings")
  @AccessWithProjectPermission(ProjectPermissionType.MANAGE)
  @Operation(summary = "Returns auto translation settings for project")
  fun getAutoTranslationSettings(): AutoTranslationSettingsDto {
    val config = autoTranslateService.getConfig(projectHolder.projectEntity)
    return AutoTranslationSettingsDto(
      usingTranslationMemory = config.usingTm,
      usingMachineTranslation = config.usingPrimaryMtService
    )
  }
}
