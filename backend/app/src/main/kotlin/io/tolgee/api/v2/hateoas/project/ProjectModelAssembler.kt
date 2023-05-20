package io.tolgee.api.v2.hateoas.project

import io.tolgee.api.v2.controllers.V2ProjectsController
import io.tolgee.api.v2.controllers.organization.OrganizationController
import io.tolgee.api.v2.hateoas.language.LanguageModelAssembler
import io.tolgee.api.v2.hateoas.organization.SimpleOrganizationModelAssembler
import io.tolgee.api.v2.hateoas.permission.ComputedPermissionModelAssembler
import io.tolgee.api.v2.hateoas.permission.PermissionModelAssembler
import io.tolgee.model.views.ProjectWithLanguagesView
import io.tolgee.service.AvatarService
import io.tolgee.service.project.ProjectService
import io.tolgee.service.security.PermissionService
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport
import org.springframework.hateoas.server.mvc.linkTo
import org.springframework.stereotype.Component

@Component
class ProjectModelAssembler(
  private val permissionService: PermissionService,
  private val projectService: ProjectService,
  private val languageModelAssembler: LanguageModelAssembler,
  private val avatarService: AvatarService,
  private val simpleOrganizationModelAssembler: SimpleOrganizationModelAssembler,
  private val permissionModelAssembler: PermissionModelAssembler,
  private val computedPermissionModelAssembler: ComputedPermissionModelAssembler
) : RepresentationModelAssemblerSupport<ProjectWithLanguagesView, ProjectModel>(
  V2ProjectsController::class.java, ProjectModel::class.java
) {
  override fun toModel(view: ProjectWithLanguagesView): ProjectModel {
    val link = linkTo<V2ProjectsController> { get(view.id) }.withSelfRel()
    val baseLanguage = view.baseLanguage ?: let {
      projectService.getOrCreateBaseLanguage(view.id)
    }

    val computedPermissions = permissionService.computeProjectPermission(
      view.organizationRole,
      view.organizationOwner.basePermission,
      view.directPermission
    )

    return ProjectModel(
      id = view.id,
      name = view.name,
      description = view.description,
      slug = view.slug,
      avatar = avatarService.getAvatarLinks(view.avatarHash),
      organizationRole = view.organizationRole,
      organizationOwner = view.organizationOwner.let { simpleOrganizationModelAssembler.toModel(it) },
      baseLanguage = baseLanguage?.let { languageModelAssembler.toModel(baseLanguage) },
      directPermission = view.directPermission?.let { permissionModelAssembler.toModel(it) },
      computedPermission = computedPermissionModelAssembler.toModel(computedPermissions),
    ).add(link).also { model ->
      view.organizationOwner?.slug?.let {
        model.add(linkTo<OrganizationController> { get(it) }.withRel("organizationOwner"))
      }
    }
  }
}
