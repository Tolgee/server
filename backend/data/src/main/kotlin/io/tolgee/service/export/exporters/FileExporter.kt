package io.tolgee.service.export.exporters

import io.tolgee.dtos.IExportParams
import io.tolgee.service.export.dataProvider.ExportTranslationView
import java.io.InputStream

interface FileExporter {
  val translations: List<ExportTranslationView>
  val exportParams: IExportParams
  val fileExtension: String

  fun produceFiles(): Map<String, InputStream>

  fun ExportTranslationView.getFilePath(namespace: String?): String {
    val filename = "${this.languageTag}.$fileExtension"
    val filePath = namespace ?: ""
    return "$filePath/$filename".replace("^/".toRegex(), "")
  }
}
