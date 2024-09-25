package io.tolgee.formats.paramConvertors.`in`

import io.tolgee.formats.ToIcuPlaceholderConvertor
import io.tolgee.formats.escapeIcu
import io.tolgee.formats.i18next.I18NEXT_UNESCAPED_FLAG_CUSTOM_KEY
import io.tolgee.formats.i18next.`in`.I18nextParameterParser
import io.tolgee.formats.i18next.`in`.PluralsI18nextKeyParser

class I18nextToIcuPlaceholderConvertor : ToIcuPlaceholderConvertor {
  private val parser = I18nextParameterParser()

  override val regex: Regex
    get() = I18NEXT_PLACEHOLDER_REGEX

  override val pluralArgName: String? = null

  private val unescapedKeys = mutableListOf<String>()

  private val _customValues = mapOf(I18NEXT_UNESCAPED_FLAG_CUSTOM_KEY to unescapedKeys)

  override val customValues: Map<String, Any>?
    get() {
      if (unescapedKeys.isEmpty()) {
        return null
      }
      return _customValues
    }

  override fun convert(
    matchResult: MatchResult,
    isInPlural: Boolean,
  ): String {
    val parsed = parser.parse(matchResult) ?: return matchResult.value.escapeIcu(isInPlural)

    if (parsed.nestedKey != null) {
      // Nested keys are not supported
      return matchResult.value.escapeIcu(isInPlural)
    }

    if (parsed.key != null && parsed.keepUnescaped) {
      unescapedKeys.add(parsed.key)
    }

    return when (parsed.format) {
      null -> "{${parsed.key}}"
      "number" -> "{${parsed.key}, number}"
      else -> matchResult.value.escapeIcu(isInPlural)
    }
  }

  companion object {
    val I18NEXT_PLACEHOLDER_REGEX =
      """
      (?x)
      (
        \{\{
        (?<unescapedflag>-\ *)?
        (?<key>\w+)(?:,\ *(?<format>[^}]+))?
        }}
        |
        \\${'$'}t\(
        (?<nestedKey>[^)]+)
        \)
      )
      """.trimIndent().toRegex()

    val I18NEXT_DETECTION_REGEX =
      """
      (?x)
      (^|\W+)
      (
        \{\{
        (?<unescapedflag>-\ *)?
        (?<key>\w+)(?:,\ *(?<format>[^}]+))?
        }}
        |
        \\${'$'}t\(
        (?<nestedKey>[^)]+)
        \)
      )
      """.trimIndent().toRegex()

    val I18NEXT_PLURAL_SUFFIX_REGEX = """^(?<key>\w+)_(?<plural>\w+)$""".toRegex()

    val I18NEXT_PLURAL_SUFFIX_KEY_PARSER = PluralsI18nextKeyParser(I18NEXT_PLURAL_SUFFIX_REGEX)
  }
}
