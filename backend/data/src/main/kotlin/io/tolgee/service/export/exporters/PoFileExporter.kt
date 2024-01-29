package io.tolgee.service.export.exporters

import io.tolgee.dtos.IExportParams
import io.tolgee.formats.po.getPluralData
import io.tolgee.formats.po.`in`.SupportedFormat
import io.tolgee.model.ILanguage
import io.tolgee.service.export.dataProvider.ExportTranslationView
import java.io.InputStream

class PoFileExporter(
  override val translations: List<ExportTranslationView>,
  override val exportParams: IExportParams,
  baseTranslationsProvider: () -> List<ExportTranslationView>,
  val baseLanguage: ILanguage,
  private val supportedFormat: SupportedFormat,
) : FileExporter {
  override val fileExtension: String = "po"

  private val preparedResult: LinkedHashMap<String, StringBuilder> = LinkedHashMap()

  override fun produceFiles(): Map<String, InputStream> {
    prepareResult()
    return preparedResult.asSequence().map { (fileName, content) ->
      fileName to content.toString().byteInputStream()
    }.toMap()
  }

  private fun prepareResult() {
    translations.forEach { translation ->
      val resultBuilder = getResultStringBuilder(translation)
      val converted =
        supportedFormat.exportMessageConverter(
          translation.text!!,
          translation.languageTag,
        ).convert()

      resultBuilder.appendLine()
      resultBuilder.writeMsgId(translation.key.name)
      resultBuilder.writeMsgStr(translation, converted)
    }
  }

  private fun StringBuilder.writeMsgId(keyName: String) {
    this.appendLine("msgid \"${keyName.escape()}\"")
  }

  private fun getResultStringBuilder(translation: ExportTranslationView): StringBuilder {
    val path = translation.getFilePath(translation.key.namespace)
    return preparedResult.computeIfAbsent(path) {
      initPoFile(translation)
    }
  }

  private fun initPoFile(translation: ExportTranslationView): StringBuilder {
    val builder = StringBuilder()
    val pluralData = getPluralData(translation.languageTag)
    builder.appendLine("msgid \"\"")
    builder.appendLine("msgstr \"\"")
    builder.appendLine("\"Language: ${translation.languageTag}\\n\"")
    builder.appendLine("\"MIME-Version: 1.0\\n\"")
    builder.appendLine("\"Content-Type: text/plain; charset=UTF-8\\n\"")
    builder.appendLine("\"Content-Transfer-Encoding: 8bit\\n\"")
    builder.appendLine("\"Plural-Forms: ${pluralData.pluralsText}\\n\"")
    builder.appendLine("\"X-Generator: Tolgee\\n\"")
    return builder
  }

  val baseTranslations by lazy {
    baseTranslationsProvider().associateBy { it.key.namespace to it.key.name }
  }

  private fun StringBuilder.writeMsgStr(
    translation: ExportTranslationView,
    converted: ConversionResult,
  ) {
    if (converted.isPlural()) {
      writePlural(translation, converted.forms)
      return
    }

    writeSingle(converted.result)
  }

  private fun StringBuilder.writePlural(
    translation: ExportTranslationView,
    forms: List<String>?,
  ) {
    forms?.forEachIndexed { index, form ->
      this.appendLine("msgstr[$index] \"${form.escape()}\"")
    }
  }

  private fun StringBuilder.writeSingle(result: String?) {
    this.appendLine("msgstr \"${result?.escape()}\"")
  }

  private fun String.escape(): String {
    return replace("\"", "\\\"").replace("\n", "\\n\"\n\"")
  }
}
