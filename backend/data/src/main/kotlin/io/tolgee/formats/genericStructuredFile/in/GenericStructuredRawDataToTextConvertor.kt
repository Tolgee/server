package io.tolgee.formats.genericStructuredFile.`in`

import com.ibm.icu.text.PluralRules
import io.tolgee.formats.ImportMessageFormat
import io.tolgee.formats.StringWrapper
import io.tolgee.formats.convertMessage
import io.tolgee.formats.forceEscapePluralForms
import io.tolgee.formats.toIcuPluralString
import java.util.*

class GenericStructuredRawDataToTextConvertor(
  private val format: ImportMessageFormat,
  private val languageTag: String? = null,
) :
  StructuredRawDataConvertor {
  private val availablePluralKeywords by lazy {
    val locale = Locale.forLanguageTag(languageTag)
    PluralRules.forLocale(locale).keywords.toSet()
  }

  override fun convert(
    rawData: Any?,
    projectIcuPlaceholdersEnabled: Boolean,
    convertPlaceholdersToIcu: Boolean,
  ): List<StructuredRawDataConversionResult>? {
    tryConvertToSingle(rawData, projectIcuPlaceholdersEnabled, convertPlaceholdersToIcu)
      ?.let {
        return it
      }
    tryConvertToPlural(rawData, projectIcuPlaceholdersEnabled, convertPlaceholdersToIcu)
      ?.let { return it }

    return null
  }

  private fun tryConvertToSingle(
    rawData: Any?,
    projectIcuPlaceholdersEnabled: Boolean,
    convertPlaceholdersToIcu: Boolean,
  ): List<StructuredRawDataConversionResult>? {
    val stringValue = getStringValue(rawData) ?: return null

    if (rawData is Number || rawData is Boolean) {
      return listOf(StructuredRawDataConversionResult(rawData.toString(), isPlural = false))
    }

    tryHandleIcuPlural(rawData, projectIcuPlaceholdersEnabled)?.let {
      return it
    }

    return convertSingleValue(
      stringValue,
      convertPlaceholdersToIcu,
      projectIcuPlaceholdersEnabled,
    )
  }

  private fun convertSingleValue(
    stringValue: String,
    convertPlaceholdersToIcu: Boolean,
    projectIcuPlaceholdersEnabled: Boolean,
  ): List<StructuredRawDataConversionResult> {
    val converted =
      convertMessage(
        message = stringValue,
        isInPlural = false,
        convertPlaceholders = convertPlaceholdersToIcu,
        isProjectIcuEnabled = projectIcuPlaceholdersEnabled,
        escapeUnmatched = !format.canContainIcu,
        convertorFactory = format.placeholderConvertorFactory,
      )

    return listOf(StructuredRawDataConversionResult(converted, null))
  }

  private fun getStringValue(rawData: Any?) =
    rawData as? String ?: (rawData as? Map<*, *>)
      ?.get(StringWrapper::_stringValue.name) as? String

  private fun tryHandleIcuPlural(
    rawData: Any?,
    projectIcuPlaceholdersEnabled: Boolean,
  ): List<StructuredRawDataConversionResult>? {
    if (format.canContainIcu && !projectIcuPlaceholdersEnabled) {
      val stringValue = getStringValue(rawData) ?: return null
      val escapedPlural = stringValue.forceEscapePluralForms()
      escapedPlural?.let {
        return listOf(StructuredRawDataConversionResult(escapedPlural, true))
      }
    }
    return null
  }

  private fun tryConvertToPlural(
    rawData: Any?,
    projectIcuPlaceholdersEnabled: Boolean,
    convertPlaceholdersToIcu: Boolean,
  ): List<StructuredRawDataConversionResult>? {
    val map = rawData as? Map<*, *> ?: return null

    if (!format.pluralsViaNesting) {
      return null
    }

    if (!map.keys.all { it in availablePluralKeywords }) {
      return null
    }

    val converted =
      map.entries.map {
        val key = it.key as? String ?: return null
        val value = it.value as? String ?: return null
        key to
          convertMessage(
            message = value,
            isInPlural = true,
            convertPlaceholders = convertPlaceholdersToIcu,
            isProjectIcuEnabled = projectIcuPlaceholdersEnabled,
            convertorFactory = format.placeholderConvertorFactory,
          )
      }.toMap().toIcuPluralString(argName = "value")

    return listOf(StructuredRawDataConversionResult(converted, true))
  }
}
