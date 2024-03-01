package io.tolgee.formats

import io.tolgee.formats.escaping.ForceIcuEscaper
import io.tolgee.formats.po.`in`.ParsedCLikeParam

/**
 * Handles the float conversion to ICU format
 * Return null if it cannot be converted reliably
 */
fun convertFloatToIcu(
  parsed: ParsedCLikeParam,
  name: String,
): String? {
  val precision = parsed.precision?.toLong() ?: 6
  val tooPrecise = precision > 50
  val usesUnsupportedFeature = usesUnsupportedFeature(parsed)
  if (tooPrecise || usesUnsupportedFeature) {
    return null
  }
  val precisionString = ".${(1..precision).joinToString("") { "0" }}"
  return "{$name, number, $precisionString}"
}

fun usesUnsupportedFeature(parsed: ParsedCLikeParam) =
  parsed.width != null || parsed.flags != null || parsed.length != null

fun convertMessage(
  message: String,
  isInPlural: Boolean,
  convertPlaceholders: Boolean,
  isProjectIcuEnabled: Boolean,
  convertorFactory: () -> ToIcuParamConvertor,
): String {
  if (!isProjectIcuEnabled && !isInPlural) return message
  if (!convertPlaceholders) return message.escapeIcu(true)

  val convertor = convertorFactory()
  return message.replaceMatchedAndUnmatched(
    string = message,
    regex = convertor.regex,
    matchedCallback = {
      convertor.convert(it, isInPlural)
    },
    unmatchedCallback = {
      ForceIcuEscaper(it, isInPlural).escaped
    },
  )
}
