package io.tolgee.api.v2.hateoas.project

import io.tolgee.api.v2.controllers.OrganizationController
import io.tolgee.api.v2.controllers.V2ProjectsController
import io.tolgee.api.v2.hateoas.organization.LanguageModelAssembler
import io.tolgee.api.v2.hateoas.user_account.UserAccountModelAssembler
import io.tolgee.model.views.ProjectView
import io.tolgee.service.PermissionService
import io.tolgee.service.ProjectService
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport
import org.springframework.hateoas.server.mvc.linkTo
import org.springframework.stereotype.Component

@Component
class ProjectModelAssembler(
  private val userAccountModelAssembler: UserAccountModelAssembler,
  private val permissionService: PermissionService,
  private val projectService: ProjectService,
  private val languageModelAssembler: LanguageModelAssembler
) : RepresentationModelAssemblerSupport<ProjectView, ProjectModel>(
  V2ProjectsController::class.java, ProjectModel::class.java
) {
  override fun toModel(view: ProjectView): ProjectModel {
    val link = linkTo<V2ProjectsController> { get(view.id) }.withSelfRel()
    val baseLanguage = view.baseLanguage ?: let {
      projectService.getOrCreateBaseLanguage(view.id)
    }
    return ProjectModel(
      id = view.id,
      name = view.name,
      description = view.description,
      slug = view.slug,
      organizationOwnerSlug = view.organizationOwnerSlug,
      organizationOwnerName = view.organizationOwnerName,
      organizationOwnerBasePermissions = view.organizationBasePermissions,
      organizationRole = view.organizationRole,
      baseLanguage = baseLanguage?.let { languageModelAssembler.toModel(baseLanguage) },
      userOwner = view.userOwner?.let { userAccountModelAssembler.toModel(it) },
      directPermissions = view.directPermissions,
      computedPermissions = permissionService.computeProjectPermissionType(
        view.organizationRole, view.organizationBasePermissions, view.directPermissions
      )
    ).add(link).also { model ->
      view.organizationOwnerSlug?.let {
        model.add(linkTo<OrganizationController> { get(it) }.withRel("organizationOwner"))
      }
    }
  }
}
