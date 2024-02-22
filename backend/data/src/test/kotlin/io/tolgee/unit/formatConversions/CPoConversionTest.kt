package io.tolgee.unit.formatConversions

import io.tolgee.formats.po.`in`.messageConvertors.PoCToIcuImportMessageConvertor
import io.tolgee.formats.po.out.c.ToCPoMessageConvertor
import io.tolgee.testing.assert
import org.junit.jupiter.api.Test

class CPoConversionTest {
  @Test
  fun `it transforms`() {
    testString("Hello %s")
    testString("Hello %d")
    testString("Hello %.2f")
    testString("Hello %f")
    testString("Hello %e")
    testString("Hello %2\$e, hello %1\$s")
    testString("Hello %.50f")
    testString("Hello %.50f")
  }

  @Test
  fun `doesn't limit precision`() {
    convertToIcu("Hello %.51f")
      .assert.isEqualTo("Hello %.51f")
  }

  private fun testString(string: String) {
    val icuString = convertToIcu(string)
    val cString = ToCPoMessageConvertor(icuString!!, forceIsPlural = false).convert().singleResult
    cString.assert
      .describedAs("Input:\n${string}\nICU:\n$icuString\nC String:\n$cString")
      .isEqualTo(string)
  }

  private fun convertToIcu(string: String) = PoCToIcuImportMessageConvertor().convert(string, "en").message
}
