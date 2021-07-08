/*
 * Copyright (c) 2020. Tolgee
 */

package io.tolgee.api.v2.controllers.translation

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import io.swagger.v3.oas.annotations.tags.Tags
import io.tolgee.api.v2.hateoas.translations.*
import io.tolgee.constants.Message
import io.tolgee.controllers.IController
import io.tolgee.dtos.PathDTO
import io.tolgee.dtos.request.GetTranslationsParams
import io.tolgee.dtos.request.SetTranslationsWithKeyDto
import io.tolgee.exceptions.BadRequestException
import io.tolgee.exceptions.NotFoundException
import io.tolgee.model.Language
import io.tolgee.model.Permission
import io.tolgee.model.enums.ApiScope
import io.tolgee.model.enums.TranslationState
import io.tolgee.model.key.Key
import io.tolgee.model.translation.Translation
import io.tolgee.security.api_key_auth.AccessWithApiKey
import io.tolgee.security.project_auth.AccessWithAnyProjectPermission
import io.tolgee.security.project_auth.AccessWithProjectPermission
import io.tolgee.security.project_auth.ProjectHolder
import io.tolgee.service.KeyService
import io.tolgee.service.LanguageService
import io.tolgee.service.TranslationService
import io.tolgee.service.query_builders.CursorUtil
import org.springdoc.api.annotations.ParameterObject
import org.springframework.data.domain.Pageable
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@Suppress("MVCPathVariableInspection", "SpringJavaInjectionPointsAutowiringInspection")
@RestController
@CrossOrigin(origins = ["*"])
@RequestMapping(value = [
    "/v2/projects/{projectId:[0-9]+}/translations",
    "/v2/projects/translations"
])
@Tags(value = [
    Tag(name = "Translations", description = "Operations related to translations in project"),
])
class V2TranslationsController(
        private val projectHolder: ProjectHolder,
        private val translationService: TranslationService,
        private val keyService: KeyService,
        private val pagedAssembler: KeysWithTranslationsPagedResourcesAssembler,
        private val keyTranslationsModelAssembler: KeyWithTranslationsModelAssembler,
        private val translationModelAssembler: TranslationModelAssembler,
        private val languageService: LanguageService
) : IController {
    @GetMapping(value = ["/{languages}"])
    @AccessWithAnyProjectPermission
    @AccessWithApiKey(scopes = [ApiScope.TRANSLATIONS_VIEW])
    @Operation(summary = "Returns all translations for specified languages", responses = [
        ApiResponse(responseCode = "200", content = [
            Content(schema = Schema(
                    example = """{"en": {"what a key": "Translated value", "another key": "Another key translated"},""" +
                            """"cs": {"what a key": "Překlad", "another key": "Další překlad"}}"""))
        ])
    ])
    fun getAllTranslations(@PathVariable("languages") languages: Set<String>): Map<String, Any> {
        return translationService.getTranslations(languages, projectHolder.project.id)
    }

    @PutMapping("")
    @AccessWithApiKey(scopes = [ApiScope.TRANSLATIONS_EDIT])
    @AccessWithProjectPermission(permission = Permission.ProjectPermissionType.TRANSLATE)
    @Operation(summary = "Sets translations for existing key")
    fun setTranslations(@RequestBody @Valid dto: SetTranslationsWithKeyDto): SetTranslationsResponseModel {
        val key = keyService.get(
                projectHolder.project.id,
                PathDTO.fromFullPath(dto.key)
        ).orElseThrow { NotFoundException() }
        val translations = translationService.setForKey(key, dto.translations)
        return getSetTranslationsResponse(key, translations)
    }

    @PostMapping("")
    @AccessWithApiKey([ApiScope.KEYS_EDIT, ApiScope.TRANSLATIONS_EDIT])
    @AccessWithProjectPermission(permission = Permission.ProjectPermissionType.EDIT)
    @Operation(summary = "Sets translations for existing or not existing key")
    fun createOrUpdateTranslations(@RequestBody @Valid dto: SetTranslationsWithKeyDto): SetTranslationsResponseModel {
        val key = keyService.getOrCreateKey(projectHolder.project, PathDTO.fromFullPath(dto.key))
        val translations = translationService.setForKey(key, dto.translations)
        return getSetTranslationsResponse(key, translations)
    }

    @PutMapping("/{translationId}/set-state/{state}")
    @AccessWithApiKey([ApiScope.TRANSLATIONS_EDIT])
    @AccessWithProjectPermission(permission = Permission.ProjectPermissionType.TRANSLATE)
    @Operation(summary = "Sets translations for existing or not existing key")
    fun setTranslationState(@PathVariable translationId: Long, @PathVariable state: TranslationState): TranslationModel {
        val translation = translationService.find(translationId) ?: throw NotFoundException()
        translation.checkFromProject()
        return translationModelAssembler.toModel(translationService.setState(translation, state))
    }

    @GetMapping(value = [""])
    @AccessWithApiKey([ApiScope.TRANSLATIONS_VIEW])
    @AccessWithProjectPermission(Permission.ProjectPermissionType.VIEW)
    @Operation(summary = "Returns translations in project")
    fun getTranslations(@ParameterObject params: GetTranslationsParams,
                        @ParameterObject pageable: Pageable): KeysWithTranslationsPageModel {
        val languages: Set<Language> = languageService
                .getLanguagesForTranslationsView(params.languages, projectHolder.project)
        val data = translationService.getViewData(projectHolder.project, pageable, params, languages)
        val cursor = if (data.content.isNotEmpty()) CursorUtil.getCursor(data.content.last(), data.sort) else null
        return pagedAssembler.toTranslationModel(data, languages, cursor)
    }

    private fun getSetTranslationsResponse(key: Key, translations: Map<String, Translation>):
            SetTranslationsResponseModel {
        return SetTranslationsResponseModel(
                keyId = key.id,
                keyName = key.name,
                translations = translations.entries.associate { (languageTag, translation) ->
                    languageTag to TranslationModel(translation.id, translation.text, translation.state)
                }
        )
    }

    private fun Translation.checkFromProject() {
        if (this.key?.project?.id != projectHolder.project.id) {
            throw BadRequestException(Message.TRANSLATION_NOT_FROM_PROJECT)
        }
    }
}
