package io.tolgee.api.v2.hateoas.user_account

import io.tolgee.api.v2.controllers.V2UserController
import io.tolgee.api.v2.hateoas.permission.ComputedPermissionModelAssembler
import io.tolgee.api.v2.hateoas.permission.PermissionModelAssembler
import io.tolgee.model.views.ExtendedUserAccountInProject
import io.tolgee.service.AvatarService
import io.tolgee.service.security.PermissionService
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport
import org.springframework.stereotype.Component

@Component
class UserAccountInProjectModelAssembler(
  private val permissionService: PermissionService,
  private val permissionModelAssembler: PermissionModelAssembler,
  private val computedPermissionModelAssembler: ComputedPermissionModelAssembler,
  private val avatarService: AvatarService
) : RepresentationModelAssemblerSupport<ExtendedUserAccountInProject, UserAccountInProjectModel>(
  V2UserController::class.java, UserAccountInProjectModel::class.java
) {
  override fun toModel(view: ExtendedUserAccountInProject): UserAccountInProjectModel {
    val computedPermissions = permissionService.computeProjectPermission(
      view.organizationRole,
      view.organizationBasePermission,
      view.directPermission,
    )
    val avatar = avatarService.getAvatarLinks(view.avatarHash)
    return UserAccountInProjectModel(
      id = view.id,
      username = view.username,
      name = view.name,
      organizationRole = view.organizationRole,
      organizationBasePermission = permissionModelAssembler.toModel(view.organizationBasePermission),
      directPermission = view.directPermission?.let { permissionModelAssembler.toModel(it) },
      computedPermission = computedPermissionModelAssembler.toModel(computedPermissions),
      avatar = avatar
    )
  }
}
