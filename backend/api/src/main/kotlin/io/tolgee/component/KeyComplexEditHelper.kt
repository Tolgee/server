package io.tolgee.component

import io.tolgee.activity.ActivityHolder
import io.tolgee.activity.data.ActivityType
import io.tolgee.constants.Message
import io.tolgee.dtos.request.key.ComplexEditKeyDto
import io.tolgee.exceptions.NotFoundException
import io.tolgee.hateoas.key.KeyWithDataModel
import io.tolgee.hateoas.key.KeyWithDataModelAssembler
import io.tolgee.model.Project
import io.tolgee.model.enums.Scope
import io.tolgee.model.enums.TranslationState
import io.tolgee.model.key.Key
import io.tolgee.model.translation.Translation
import io.tolgee.security.ProjectHolder
import io.tolgee.service.LanguageService
import io.tolgee.service.key.KeyService
import io.tolgee.service.key.ScreenshotService
import io.tolgee.service.key.TagService
import io.tolgee.service.security.SecurityService
import io.tolgee.service.translation.TranslationService
import io.tolgee.util.executeInNewTransaction
import io.tolgee.util.getSafeNamespace
import org.springframework.context.ApplicationContext
import org.springframework.transaction.PlatformTransactionManager
import kotlin.properties.Delegates

class KeyComplexEditHelper(
  applicationContext: ApplicationContext,
  private val keyId: Long,
  private val dto: ComplexEditKeyDto
) {
  private val keyWithDataModelAssembler: KeyWithDataModelAssembler =
    applicationContext.getBean(KeyWithDataModelAssembler::class.java)
  private val keyService: KeyService = applicationContext.getBean(KeyService::class.java)
  private val securityService: SecurityService = applicationContext.getBean(SecurityService::class.java)
  private val languageService: LanguageService = applicationContext.getBean(LanguageService::class.java)
  private val projectHolder: ProjectHolder = applicationContext.getBean(ProjectHolder::class.java)
  private val translationService: TranslationService = applicationContext.getBean(TranslationService::class.java)
  private val tagService: TagService = applicationContext.getBean(TagService::class.java)
  private val screenshotService: ScreenshotService = applicationContext.getBean(ScreenshotService::class.java)
  private val activityHolder: ActivityHolder = applicationContext.getBean(ActivityHolder::class.java)
  private val transactionManager: PlatformTransactionManager =
    applicationContext.getBean(PlatformTransactionManager::class.java)

  private lateinit var key: Key
  private var modifiedTranslations: Map<Long, String?>? = null
  private var modifiedStates: Map<Long, TranslationState>? = mapOf()
  private val dtoTags = dto.tags

  private var areTranslationsModified by Delegates.notNull<Boolean>()
  private var areStatesModified by Delegates.notNull<Boolean>()
  private var areTagsModified by Delegates.notNull<Boolean>()
  private var isKeyModified by Delegates.notNull<Boolean>()
  private var isScreenshotDeleted by Delegates.notNull<Boolean>()
  private var isScreenshotAdded by Delegates.notNull<Boolean>()

  private val languages by lazy {
    val translationLanguages = dto.translations?.keys ?: setOf()
    val stateLanguages = dto.states?.keys ?: setOf()

    val all = (translationLanguages + stateLanguages)
    if (all.isEmpty()) {
      return@lazy setOf()
    }
    languageService.findByTags(all, projectHolder.project.id)
  }

  private val existingTranslations: MutableMap<String, Translation> by lazy {
    translationService.getKeyTranslations(
      languages,
      projectHolder.projectEntity,
      key
    ).associateBy { it.language.tag }.toMutableMap()
  }

  fun doComplexUpdate(): KeyWithDataModel {
    return executeInNewTransaction(transactionManager = transactionManager) {
      prepareData()
      prepareConditions()
      setActivityHolder()

      doTranslationUpdate()
      doStateUpdate()
      doUpdateTags()
      doUpdateScreenshots()
      doUpdateKey()
    }
  }

  private fun doUpdateKey(): KeyWithDataModel {
    var edited = key

    if (isKeyModified) {
      key.project.checkKeysEditPermission()
      edited = keyService.edit(key, dto.name, dto.namespace)
    }

    return keyWithDataModelAssembler.toModel(edited)
  }

  private fun doUpdateScreenshots() {
    if (isScreenshotAdded || isScreenshotDeleted) {
      updateScreenshotsWithPermissionCheck(dto, key)
    }
  }

  private fun doUpdateTags() {
    if (dtoTags !== null && areTagsModified) {
      key.project.checkKeysEditPermission()
      tagService.updateTags(key, dtoTags)
    }
  }

  private fun doStateUpdate() {
    if (areStatesModified) {
      securityService.checkLanguageChangeStatePermissionsByLanguageId(modifiedStates!!.keys, projectHolder.project.id)
      translationService.setStateBatch(
        states = modifiedStates!!.map {
          val translation = existingTranslations[languageById(it.key).tag] ?: throw NotFoundException(
            Message.TRANSLATION_NOT_FOUND
          )

          translation to it.value
        }.toMap()
      )
    }
  }

  private fun doTranslationUpdate() {
    if (modifiedTranslations != null && areTranslationsModified) {
      projectHolder.projectEntity.checkTranslationsEditPermission()
      securityService.checkLanguageTranslatePermissionsByLanguageId(
        modifiedTranslations!!.keys,
        projectHolder.project.id
      )

      val modifiedTranslations = getModifiedTranslationsByTag()
      val existingTranslationsByTag = getExistingTranslationsByTag()
      val oldTranslations = modifiedTranslations.map {
        it.key to existingTranslationsByTag[it.key]
      }.toMap()

      val translations = translationService.setForKey(
        key,
        oldTranslations = oldTranslations,
        translations = modifiedTranslations
      )

      translations.forEach {
        if (existingTranslations[it.key.tag] == null) {
          existingTranslations[it.key.tag] = it.value
        }
      }
    }
  }

  private fun getExistingTranslationsByTag() =
    existingTranslations.map { languageByTag(it.key) to it.value.text }.toMap()

  private fun getModifiedTranslationsByTag() = modifiedTranslations!!
    .map { languageById(it.key) to it.value }
    .toMap()

  private fun setActivityHolder() {
    if (!isSingleOperation) {
      activityHolder.activity = ActivityType.COMPLEX_EDIT
      return
    }

    if (areTranslationsModified) {
      activityHolder.activity = ActivityType.SET_TRANSLATIONS
      return
    }

    if (areStatesModified) {
      activityHolder.activity = ActivityType.SET_TRANSLATION_STATE
      return
    }

    if (areTagsModified) {
      activityHolder.activity = ActivityType.KEY_TAGS_EDIT
      return
    }

    if (isKeyModified) {
      activityHolder.activity = ActivityType.KEY_NAME_EDIT
      return
    }

    if (isScreenshotAdded) {
      activityHolder.activity = ActivityType.SCREENSHOT_ADD
      return
    }

    if (isScreenshotDeleted) {
      activityHolder.activity = ActivityType.SCREENSHOT_DELETE
      return
    }
  }

  private val isSingleOperation: Boolean
    get() {
      return arrayOf(
        areTranslationsModified,
        areStatesModified,
        areTagsModified,
        isKeyModified,
        isScreenshotAdded,
        isScreenshotDeleted
      ).sumOf { if (it) 1 as Int else 0 } == 0
    }

  private fun prepareData() {
    key = keyService.get(keyId)
    key.checkInProject()
    prepareModifiedTranslations()
    prepareModifiedStates()
  }

  private fun prepareConditions() {
    areTranslationsModified = !modifiedTranslations.isNullOrEmpty()
    areStatesModified = !modifiedStates.isNullOrEmpty()
    areTagsModified = dtoTags != null && areTagsModified(key, dtoTags)
    isKeyModified = key.name != dto.name || getSafeNamespace(key.namespace?.name) != getSafeNamespace(dto.namespace)
    isScreenshotDeleted = !dto.screenshotIdsToDelete.isNullOrEmpty()
    isScreenshotAdded = !dto.screenshotUploadedImageIds.isNullOrEmpty() || !dto.screenshotsToAdd.isNullOrEmpty()
  }

  private fun areTagsModified(
    key: Key,
    dtoTags: List<String>
  ): Boolean {
    val currentTags = key.keyMeta?.tags?.map { it.name } ?: listOf()
    val currentTagsContainAllNewTags = currentTags.containsAll(dtoTags)
    val newTagsContainAllCurrentTags = dtoTags.containsAll(currentTags)

    return !currentTagsContainAllNewTags || !newTagsContainAllCurrentTags
  }

  private fun prepareModifiedTranslations() {
    modifiedTranslations = dto.translations?.filter { it.value != existingTranslations[it.key]?.text }
      ?.mapKeys { languageByTag(it.key).id }
  }

  private fun prepareModifiedStates() {
    modifiedStates = dto.states?.filter { it.value.translationState != existingTranslations[it.key]?.state }
      ?.map { languageByTag(it.key).id to it.value.translationState }?.toMap()
  }

  private fun languageByTag(tag: String): io.tolgee.model.Language {
    return languages.find { it.tag == tag } ?: throw NotFoundException(Message.LANGUAGE_NOT_FOUND)
  }

  private fun languageById(id: Long): io.tolgee.model.Language {
    return languages.find { it.id == id } ?: throw NotFoundException(Message.LANGUAGE_NOT_FOUND)
  }

  private fun updateScreenshotsWithPermissionCheck(dto: ComplexEditKeyDto, key: Key) {
    dto.screenshotIdsToDelete?.let { screenshotIds ->
      deleteScreenshots(screenshotIds, key)
    }

    addScreenshots(key, dto)
  }

  private fun addScreenshots(key: Key, dto: ComplexEditKeyDto) {
    if (isScreenshotAdded) {
      key.project.checkScreenshotsUploadPermission()
    }

    val screenshotUploadedImageIds = dto.screenshotUploadedImageIds
    if (screenshotUploadedImageIds != null) {
      screenshotService.saveUploadedImages(screenshotUploadedImageIds, key)
      return
    }

    val screenshotsToAdd = dto.screenshotsToAdd
    if (screenshotsToAdd != null) {
      screenshotService.saveUploadedImages(screenshotsToAdd, key)
    }
  }

  private fun deleteScreenshots(
    screenshotIds: List<Long>,
    key: Key
  ) {
    if (screenshotIds.isNotEmpty()) {
      key.project.checkScreenshotsDeletePermission()
    }
    screenshotService.findByIdIn(screenshotIds).forEach {
      screenshotService.removeScreenshotReference(key, it)
    }
  }

  private fun Project.checkScreenshotsDeletePermission() {
    securityService.checkProjectPermission(this.id, Scope.SCREENSHOTS_DELETE)
  }

  private fun Project.checkKeysEditPermission() {
    securityService.checkProjectPermission(this.id, Scope.KEYS_EDIT)
  }

  private fun Project.checkTranslationsEditPermission() {
    securityService.checkProjectPermission(this.id, Scope.TRANSLATIONS_EDIT)
  }

  private fun Key.checkInProject() {
    keyService.checkInProject(this, projectHolder.project.id)
  }

  private fun Project.checkScreenshotsUploadPermission() {
    securityService.checkScreenshotsUploadPermission(this.id)
  }
}
