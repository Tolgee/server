package io.tolgee.formats.genericStructuredFile.out

import com.fasterxml.jackson.databind.ObjectMapper
import io.tolgee.dtos.IExportParams
import io.tolgee.formats.FromIcuPlaceholderConvertor
import io.tolgee.formats.NoOpFromIcuPlaceholderConvertor
import io.tolgee.formats.generic.IcuToGenericFormatMessageConvertor
import io.tolgee.formats.nestedStructureModel.StructureModelBuilder
import io.tolgee.service.export.dataProvider.ExportTranslationView
import io.tolgee.service.export.exporters.FileExporter
import java.io.InputStream

class GenericStructuredFileExporter(
  override val translations: List<ExportTranslationView>,
  override val exportParams: IExportParams,
  override val fileExtension: String,
  private val projectIcuPlaceholdersSupport: Boolean,
  private val objectMapper: ObjectMapper,
  private val placeholderConvertorFactory: (() -> FromIcuPlaceholderConvertor)?,
  private val rootKeyIsLanguageTag: Boolean = false,
  private val pluralsViaNesting: Boolean = false,
) : FileExporter {
  val result: LinkedHashMap<String, StructureModelBuilder> = LinkedHashMap()

  override fun produceFiles(): Map<String, InputStream> {
    prepare()
    return result.asSequence().map { (fileName, modelBuilder) ->
      fileName to
        objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(modelBuilder.result)
          .inputStream()
    }.toMap()
  }

  private fun prepare() {
    translations.forEach { translation ->
      addTranslationToBuilder(translation)
    }
  }

  private fun addTranslationToBuilder(translation: ExportTranslationView) {
    if (translation.key.isPlural) {
      addPluralTranslation(translation)
      return
    }
    addSingularTranslation(translation)
  }

  private fun addSingularTranslation(translation: ExportTranslationView) {
    val builder = getFileContentResultBuilder(translation)
    builder.addValue(
      translation.languageTag,
      translation.key.name,
      convertMessage(translation.text, translation.key.isPlural),
    )
  }

  private fun addPluralTranslation(translation: ExportTranslationView) {
    if (pluralsViaNesting) {
      addNestedPlural(translation)
    }
    return addSingularTranslation(translation)
  }

  private fun addNestedPlural(translation: ExportTranslationView) {
    val pluralForms =
      convertMessageForNestedPlural(translation.text) ?: let {
        // this should never happen, but if it does, it's better to add a null key then crash or ignore it
        addNullValue(translation)
        return
      }

    val builder = getFileContentResultBuilder(translation)
    builder.addValue(
      translation.languageTag,
      translation.key.name,
      pluralForms,
    )
  }

  private fun addNullValue(translation: ExportTranslationView) {
    val builder = getFileContentResultBuilder(translation)
    builder.addValue(
      translation.languageTag,
      translation.key.name,
      null,
    )
  }

  private fun convertMessage(
    text: String?,
    isPlural: Boolean,
  ): String? {
    return getMessageConvertor(text, isPlural).convert()
  }

  private fun getMessageConvertor(
    text: String?,
    isPlural: Boolean,
  ) = IcuToGenericFormatMessageConvertor(
    text,
    isPlural,
    projectIcuPlaceholdersSupport,
    placeholderConvertorFactory ?: { NoOpFromIcuPlaceholderConvertor() },
  )

  private fun convertMessageForNestedPlural(text: String?): Map<String, String>? {
    return getMessageConvertor(text, true).getForcedPluralForms()
  }

  private fun getFileContentResultBuilder(translation: ExportTranslationView): StructureModelBuilder {
    val absolutePath = translation.getFilePath()
    return result.computeIfAbsent(absolutePath) {
      StructureModelBuilder(
        structureDelimiter = exportParams.structureDelimiter,
        supportJsonArrays = exportParams.supportArrays,
        rootKeyIsLanguageTag = rootKeyIsLanguageTag,
      )
    }
  }
}
