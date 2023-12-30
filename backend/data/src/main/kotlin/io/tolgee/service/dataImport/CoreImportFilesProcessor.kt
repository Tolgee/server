package io.tolgee.service.dataImport

import io.tolgee.configuration.tolgee.TolgeeProperties
import io.tolgee.dtos.dataImport.ImportAddFilesParams
import io.tolgee.dtos.dataImport.ImportFileDto
import io.tolgee.exceptions.ErrorResponseBody
import io.tolgee.exceptions.ImportCannotParseFileException
import io.tolgee.model.dataImport.Import
import io.tolgee.model.dataImport.ImportFile
import io.tolgee.model.dataImport.ImportKey
import io.tolgee.model.dataImport.ImportTranslation
import io.tolgee.model.dataImport.issues.issueTypes.FileIssueType
import io.tolgee.model.dataImport.issues.paramTypes.FileIssueParamType
import io.tolgee.service.dataImport.processors.FileProcessorContext
import io.tolgee.service.dataImport.processors.ProcessorFactory
import io.tolgee.util.Logging
import org.springframework.context.ApplicationContext

class CoreImportFilesProcessor(
  val applicationContext: ApplicationContext,
  val import: Import,
  val params: ImportAddFilesParams = ImportAddFilesParams(),
) : Logging {
  private val importService: ImportService by lazy { applicationContext.getBean(ImportService::class.java) }
  private val processorFactory: ProcessorFactory by lazy { applicationContext.getBean(ProcessorFactory::class.java) }
  private val tolgeeProperties: TolgeeProperties by lazy { applicationContext.getBean(TolgeeProperties::class.java) }

  private val importDataManager by lazy {
    ImportDataManager(
      applicationContext = applicationContext,
      import = import,
    )
  }

  fun processFiles(files: Collection<ImportFileDto>?): MutableList<ErrorResponseBody> {
    val errors = mutableListOf<ErrorResponseBody>()
    files?.forEach {
      try {
        val newErrors = processFileOrArchive(it)
        errors.addAll(newErrors)
      } catch (e: ImportCannotParseFileException) {
        errors.add(ErrorResponseBody(e.code, e.params))
      }
    }

    importDataManager.handleConflicts(false)
    return errors
  }

  private fun processFileOrArchive(file: ImportFileDto): MutableList<ErrorResponseBody> {
    val errors = mutableListOf<ErrorResponseBody>()

    if (file.isArchive) {
      return processArchive(file, errors)
    }

    processFile(file)
    return mutableListOf()
  }

  private fun processFile(file: ImportFileDto) {
    val savedFileEntity = file.saveFileEntity()
    val fileProcessorContext =
      FileProcessorContext(
        file = file,
        fileEntity = savedFileEntity,
        maxTranslationTextLength = tolgeeProperties.maxTranslationTextLength,
        params = params,
      )
    val processor = processorFactory.getProcessor(file, fileProcessorContext)
    processor.process()
    processor.context.processResult()
  }

  private fun processArchive(
    archive: ImportFileDto,
    errors: MutableList<ErrorResponseBody>,
  ): MutableList<ErrorResponseBody> {
    val processor = processorFactory.getArchiveProcessor(archive)
    val files = processor.process(archive)

    errors.addAll(processFiles(files))
    return errors
  }

  private val ImportFileDto.isArchive: Boolean
    get() {
      return this.name.endsWith(".zip")
    }

  private fun ImportFileDto.saveFileEntity(): ImportFile {
    val entity =
      ImportFile(
        name,
        import,
      )
    import.files.add(entity)
    return importService.saveFile(entity)
  }

  private fun FileProcessorContext.processResult() {
    this.fileEntity.preselectNamespace()
    this.processLanguages()
    this.processTranslations()
    importService.saveAllFileIssues(this.fileEntity.issues)
    importService.saveAllFileIssueParams(this.fileEntity.issues.flatMap { it.params ?: emptyList() })
  }

  private fun ImportFile.preselectNamespace() {
    val namespace = """^[\/]?([^/\\]+)[/\\].*""".toRegex().matchEntire(this.name!!)?.groups?.get(1)?.value
    if (!namespace.isNullOrBlank()) {
      this.namespace = namespace
    }
  }

  private fun FileProcessorContext.processLanguages() {
    this.languages.forEach { entry ->
      val languageEntity = entry.value
      importDataManager.storedLanguages.add(languageEntity)
      languageEntity.existingLanguage = importDataManager.findMatchingExistingLanguage(languageEntity)
      importService.saveLanguages(this.languages.values)
      importDataManager.populateStoredTranslations(entry.value)
    }
  }

  private fun addToStoredTranslations(translation: ImportTranslation) {
    importDataManager.storedTranslations[translation.language]!!.let { it[translation.key]!!.add(translation) }
  }

  private fun FileProcessorContext.getOrCreateKey(name: String): ImportKey {
    return importDataManager.storedKeys.computeIfAbsent(this.fileEntity to name) {
      this.keys.computeIfAbsent(name) {
        ImportKey(name = name, this.fileEntity)
      }.also {
        importService.saveKey(it)
      }
    }
  }

  private fun FileProcessorContext.processTranslations() {
    this.translations.forEach { entry ->
      val keyEntity = getOrCreateKey(entry.key)
      entry.value.forEach translationForeach@{ newTranslation ->
        val storedTranslations = importDataManager.getStoredTranslations(keyEntity, newTranslation.language)
        newTranslation.key = keyEntity
        if (storedTranslations.size > 0) {
          storedTranslations.forEach { collidingTranslations ->
            fileEntity.addIssue(
              FileIssueType.MULTIPLE_VALUES_FOR_KEY_AND_LANGUAGE,
              mapOf(
                FileIssueParamType.KEY_ID to collidingTranslations.key.id.toString(),
                FileIssueParamType.LANGUAGE_ID to collidingTranslations.language.id.toString(),
              ),
            )
          }
          return@translationForeach
        }
        this@CoreImportFilesProcessor.addToStoredTranslations(newTranslation)
      }
    }
    importDataManager.saveAllStoredKeys()
    importDataManager.saveAllStoredTranslations()
  }
}
