package io.tolgee.helpers

import java.util.regex.MatchResult
import java.util.regex.Pattern

object TextHelper {
  @JvmStatic
  fun splitOnNonEscapedDelimiter(string: String, delimiter: Char?): List<String> {
    if (delimiter == null) {
      return listOf(string)
    }

    val result = ArrayList<String>()
    var actual = StringBuilder()
    for (i in string.indices) {
      val character = string[i]
      if (character == delimiter && !isCharEscaped(i, string)) {
        result.add(removeEscapes(actual.toString()))
        actual = StringBuilder()
        continue
      }
      actual.append(string[i])
    }
    result.add(removeEscapes(actual.toString()))
    return result
  }

  private fun isCharEscaped(position: Int, fullString: String): Boolean {
    if (position == 0) {
      return false
    }
    var pos = position
    var escapeCharsCount = 0
    while (pos > -1 && fullString[pos - 1] == '\\') {
      escapeCharsCount++
      pos--
    }
    return escapeCharsCount % 2 == 1
  }

  private fun removeEscapes(text: String): String {
    return Pattern.compile("\\\\?\\\\?").matcher(text).replaceAll { match: MatchResult ->
      if (match.group() == "\\\\") {
        // this seems strange. We need to escape it once more for the replace logic
        return@replaceAll "\\\\"
      }
      ""
    }
  }
}
