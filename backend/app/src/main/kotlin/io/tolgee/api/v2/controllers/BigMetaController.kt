package io.tolgee.api.v2.controllers

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import io.tolgee.api.v2.hateoas.bigMeta.BigMetaModelAssembler
import io.tolgee.api.v2.hateoas.language.BigMetaModel
import io.tolgee.dtos.BigMetaDto
import io.tolgee.model.enums.Scope
import io.tolgee.model.views.BigMetaView
import io.tolgee.security.apiKeyAuth.AccessWithApiKey
import io.tolgee.security.project_auth.AccessWithProjectPermission
import io.tolgee.security.project_auth.ProjectHolder
import io.tolgee.service.BigMetaService
import org.springdoc.api.annotations.ParameterObject
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PagedResourcesAssembler
import org.springframework.hateoas.PagedModel
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.validation.Valid

@Suppress("MVCPathVariableInspection")
@RestController
@RequestMapping(
  value = [
    "/v2/projects/{projectId:\\d+}",
    "/v2/projects"
  ]
)
@Tag(name = "Big Meta data about the keys in project")
class BigMetaController(
  private val bigMetaService: BigMetaService,
  private val bigMetaModelAssembler: BigMetaModelAssembler,
  private val projectHolder: ProjectHolder,
  private val pageAssembler: PagedResourcesAssembler<BigMetaView>,
) {
  @PostMapping("/big-meta")
  @Operation(summary = "Stores a bigMeta for a project")
  @AccessWithApiKey
  @AccessWithProjectPermission(Scope.TRANSLATIONS_EDIT)
  fun store(@RequestBody @Valid data: BigMetaDto): List<BigMetaModel> {
    val stored = bigMetaService.store(data, projectHolder.projectEntity)
    return bigMetaModelAssembler.toCollectionModel(stored).toList()
  }

  @GetMapping("/keys/{keyId:\\d+}/big-meta")
  @AccessWithProjectPermission(Scope.KEYS_VIEW)
  fun listForKey(@PathVariable keyId: Long, @ParameterObject pageable: Pageable): PagedModel<BigMetaModel> {
    val data = bigMetaService.getAllForKeyPaged(projectHolder.project.id, keyId, pageable)
    return pageAssembler.toModel(data, bigMetaModelAssembler)
  }
}
