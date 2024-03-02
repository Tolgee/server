package io.tolgee.formats.apple.`in`

import io.tolgee.formats.ImportMessageConvertor
import io.tolgee.formats.MessageConvertorResult
import io.tolgee.formats.convertMessage
import io.tolgee.formats.toIcuPluralString

class AppleToIcuMessageConvertor : ImportMessageConvertor {
  @Suppress("UNCHECKED_CAST")
  override fun convert(
    rawData: Any?,
    languageTag: String,
    convertPlaceholders: Boolean,
    isProjectIcuEnabled: Boolean,
  ): MessageConvertorResult {
    val stringValue = rawData as? String ?: (rawData as? Map<*, *>)?.get("_stringValue") as? String

    if (stringValue is String) {
      val converted = convert(stringValue, false, convertPlaceholders, isProjectIcuEnabled)
      return MessageConvertorResult(converted, false)
    }

    if (rawData is Map<*, *>) {
      val rawDataMap = rawData as Map<String, String>
      val converted = convertPlural(rawDataMap, convertPlaceholders, isProjectIcuEnabled)
      return MessageConvertorResult(converted, true)
    }

    if (rawData == null) {
      return MessageConvertorResult(null, false)
    }

    throw IllegalArgumentException("Unsupported type of message")
  }

  private fun convertPlural(
    rawData: Map<String, String>,
    convertPlaceholders: Boolean,
    isProjectIcuEnabled: Boolean,
  ): String {
    val converted =
      rawData.mapNotNull {
        it.key to convert(it.value, true, convertPlaceholders, isProjectIcuEnabled)
      }.toMap().toIcuPluralString(optimize = true, addNewLines = true, argName = "0")
    return converted
  }

  private fun convert(
    message: String,
    isInPlural: Boolean = false,
    convertPlaceholders: Boolean,
    isProjectIcuEnabled: Boolean,
  ): String {
    return convertMessage(message, isInPlural, convertPlaceholders, isProjectIcuEnabled) {
      AppleToIcuParamConvertor()
    }
  }
}
