/*
 * Copyright (c) 2020. Tolgee
 */

package io.tolgee.api.v2.controllers

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import io.tolgee.api.v2.hateoas.repository.RepositoryModel
import io.tolgee.api.v2.hateoas.repository.RepositoryModelAssembler
import io.tolgee.api.v2.hateoas.user_account.UserAccountInProjectModel
import io.tolgee.api.v2.hateoas.user_account.UserAccountInRepositoryModelAssembler
import io.tolgee.constants.Message
import io.tolgee.exceptions.BadRequestException
import io.tolgee.model.Permission
import io.tolgee.model.views.ProjectView
import io.tolgee.model.views.UserAccountInProjectView
import io.tolgee.security.AuthenticationFacade
import io.tolgee.security.api_key_auth.AccessWithApiKey
import io.tolgee.security.repository_auth.AccessWithAnyRepositoryPermission
import io.tolgee.security.repository_auth.AccessWithRepositoryPermission
import io.tolgee.security.repository_auth.RepositoryHolder
import io.tolgee.service.PermissionService
import io.tolgee.service.RepositoryService
import io.tolgee.service.UserAccountService
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PagedResourcesAssembler
import org.springframework.hateoas.MediaTypes
import org.springframework.hateoas.PagedModel
import org.springframework.web.bind.annotation.*

@RestController
@CrossOrigin(origins = ["*"])
@RequestMapping(value = ["/v2/repositories"])
@Tag(name = "Repositories")
open class V2RepositoriesController(
        val repositoryService: RepositoryService,
        val repositoryHolder: RepositoryHolder,
        @Suppress("SpringJavaInjectionPointsAutowiringInspection")
        val arrayResourcesAssembler: PagedResourcesAssembler<ProjectView>,
        @Suppress("SpringJavaInjectionPointsAutowiringInspection")
        val userArrayResourcesAssembler: PagedResourcesAssembler<UserAccountInProjectView>,
        val userAccountInRepositoryModelAssembler: UserAccountInRepositoryModelAssembler,
        val repositoryModelAssembler: RepositoryModelAssembler,
        val userAccountService: UserAccountService,
        val permissionService: PermissionService,
        val authenticationFacade: AuthenticationFacade
) {
    @Operation(summary = "Returns all repositories, which are current user permitted to view")
    @GetMapping("", produces = [MediaTypes.HAL_JSON_VALUE])
    open fun getAll(pageable: Pageable, @RequestParam("search") search: String?): PagedModel<RepositoryModel>? {
        val repositories = repositoryService.findPermittedPaged(pageable, search)
        return arrayResourcesAssembler.toModel(repositories, repositoryModelAssembler)
    }

    @GetMapping("/{repositoryId}")
    @AccessWithAnyRepositoryPermission
    @AccessWithApiKey
    @Operation(summary = "Returns repository by id")
    open fun get(@PathVariable("repositoryId") repositoryId: Long): RepositoryModel {
        return repositoryService.getView(repositoryId)?.let {
            repositoryModelAssembler.toModel(it)
        } ?: throw io.tolgee.exceptions.NotFoundException()
    }

    @GetMapping("/{repositoryId}/users")
    @Operation(summary = "Returns repository all users, who have permission to access repository")
    @AccessWithRepositoryPermission(Permission.ProjectPermissionType.MANAGE)
    open fun getAllUsers(@PathVariable("repositoryId") repositoryId: Long,
                         pageable: Pageable,
                         @RequestParam("search", required = false) search: String?
    ): PagedModel<UserAccountInProjectModel> {
        return userAccountService.getAllInRepository(repositoryId, pageable, search).let { users ->
            userArrayResourcesAssembler.toModel(users, userAccountInRepositoryModelAssembler)
        }
    }

    @PutMapping("/{repositoryId}/users/{userId}/set-permissions/{permissionType}")
    @AccessWithRepositoryPermission(Permission.ProjectPermissionType.MANAGE)
    @Operation(summary = "Sets user's direct permission")
    open fun setUsersPermissions(
            @PathVariable("repositoryId") repositoryId: Long,
            @PathVariable("userId") userId: Long,
            @PathVariable("permissionType") permissionType: Permission.ProjectPermissionType,
    ) {
        if (userId == authenticationFacade.userAccount.id) {
            throw BadRequestException(Message.CANNOT_SET_YOUR_OWN_PERMISSIONS)
        }
        permissionService.setUserDirectPermission(repositoryId, userId, permissionType)
    }

    @PutMapping("/{repositoryId}/users/{userId}/revoke-access")
    @AccessWithRepositoryPermission(Permission.ProjectPermissionType.MANAGE)
    @Operation(summary = "Revokes user's access")
    fun revokePermission(
            @PathVariable("repositoryId") repositoryId: Long,
            @PathVariable("userId") userId: Long
    ) {
        if (userId == authenticationFacade.userAccount.id) {
            throw BadRequestException(Message.CAN_NOT_REVOKE_OWN_PERMISSIONS)
        }
        permissionService.revoke(repositoryId, userId)
    }

}
