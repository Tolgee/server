package io.tolgee.formats.android.`in`

import io.tolgee.formats.ToIcuParamConvertor
import io.tolgee.formats.convertFloatToIcu
import io.tolgee.formats.escapeIcu
import io.tolgee.formats.po.`in`.CLikeParameterParser
import io.tolgee.formats.usesUnsupportedFeature

class JavaToIcuParamConvertor() : ToIcuParamConvertor {
  private val parser = CLikeParameterParser()
  private var index = 0

  override val regex: Regex
    get() = JAVA_PLACEHOLDER_REGEX

  override fun convert(
    matchResult: MatchResult,
    isInPlural: Boolean,
  ): String {
    index++
    val parsed = parser.parse(matchResult) ?: return matchResult.value.escapeIcu(isInPlural)

    if (usesUnsupportedFeature(parsed)) {
      return parsed.fullMatch.escapeIcu(isInPlural)
    }

    if (parsed.specifier == "%") {
      return "%"
    }

    val zeroIndexedArgNum = parsed.argNum?.toIntOrNull()?.minus(1)
    val name = zeroIndexedArgNum?.toString() ?: ((index - 1).toString())
    val isValidPluralReplaceNumber = parsed.specifier == "d" && name == "0"

    if (isInPlural && isValidPluralReplaceNumber) {
      return "#"
    }

    if (isValidPluralReplaceNumber) {
      return "{$name, number}"
    }

    when (parsed.specifier) {
      "s" -> return "{$name}"
      "e" -> return "{$name, number, scientific}"
      "d" -> return "{$name, number}"
      "f" -> return convertFloatToIcu(parsed, name) ?: parsed.fullMatch.escapeIcu(isInPlural)
    }

    return parsed.fullMatch.escapeIcu(isInPlural)
  }

  companion object {
    val JAVA_PLACEHOLDER_REGEX =
      """
      (?x)(
      %
      (?:(?<argnum>\d+)${"\\$"})?
      (?<flags>[-\#+\s0,(]+)?
      (?<width>\d+)?
      (?:\.(?<precision>\d+))?
      (?<specifier>[bBhHsScCdoxXeEfgGaAtT%nRrDF])
      )
      """.trimIndent().toRegex()
  }
}
