package io.tolgee.ee.api.v2.controllers

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import io.tolgee.ee.api.v2.hateoas.cdnStorage.CdnStorageModel
import io.tolgee.ee.api.v2.hateoas.cdnStorage.CdnStorageModelAssembler
import io.tolgee.ee.data.CdnStorageDto
import io.tolgee.ee.data.StorageTestResult
import io.tolgee.ee.service.CdnStorageService
import io.tolgee.model.cdn.CdnStorage
import io.tolgee.model.enums.Scope
import io.tolgee.security.ProjectHolder
import io.tolgee.security.authentication.AllowApiAccess
import io.tolgee.security.authorization.RequiresProjectPermissions
import org.springdoc.api.annotations.ParameterObject
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PagedResourcesAssembler
import org.springframework.hateoas.PagedModel
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.validation.Valid

@Suppress("MVCPathVariableInspection")
@RestController
@CrossOrigin(origins = ["*"])
@RequestMapping(
  value = [
    "/v2/projects/{projectId}/cdn-storages",
  ]
)
@Tag(name = "Cdn management", description = "Endpoints for CDN management")
class CdnStorageController(
  private val cdnStorageService: CdnStorageService,
  private val projectHolder: ProjectHolder,
  private val cdnStorageModelAssembler: CdnStorageModelAssembler,
  private val pageModelAssembler: PagedResourcesAssembler<CdnStorage>,
) {
  @PostMapping("")
  @Operation(description = "Create CDN Storage")
  @RequiresProjectPermissions([Scope.CDN_MANAGE])
  @AllowApiAccess
  fun createCdnStorage(@Valid @RequestBody dto: CdnStorageDto): CdnStorageModel {
    val cdnStorage = cdnStorageService.create(projectHolder.project.id, dto)
    return cdnStorageModelAssembler.toModel(cdnStorage)
  }

  @PutMapping("/{cdnId}")
  @Operation(description = "Updates CDN Storage")
  @RequiresProjectPermissions([Scope.CDN_MANAGE])
  @AllowApiAccess
  fun update(@PathVariable cdnId: Long, @Valid @RequestBody dto: CdnStorageDto): CdnStorageModel {
    val cdnStorage = cdnStorageService.update(cdnId, dto)
    return cdnStorageModelAssembler.toModel(cdnStorage)
  }

  @RequiresProjectPermissions([Scope.CDN_MANAGE])
  @GetMapping("")
  @Operation(description = "List existing CDNs Storages")
  @AllowApiAccess
  fun list(@ParameterObject pageable: Pageable): PagedModel<CdnStorageModel> {
    val page = cdnStorageService.getAllInProject(projectHolder.project.id, pageable)
    return pageModelAssembler.toModel(page, cdnStorageModelAssembler)
  }

  @RequiresProjectPermissions([Scope.CDN_MANAGE])
  @DeleteMapping("/{cdnId}")
  @Operation(description = "Delete CDN Storage")
  @AllowApiAccess
  fun delete(@PathVariable cdnId: Long) {
    cdnStorageService.delete(cdnId)
  }

  @RequiresProjectPermissions([Scope.CDN_MANAGE])
  @GetMapping("/{cdnId}")
  @Operation(description = "Get CDN Storage")
  @AllowApiAccess
  fun get(@PathVariable cdnId: Long): CdnStorageModel {
    return cdnStorageModelAssembler.toModel(cdnStorageService.get(cdnId))
  }

  @RequiresProjectPermissions([Scope.CDN_MANAGE])
  @GetMapping("/test")
  @Operation(description = "Test CDN Storage")
  @AllowApiAccess
  fun test(@Valid @RequestBody dto: CdnStorageDto): StorageTestResult {
    return cdnStorageService.testStorage(dto)
  }
}
