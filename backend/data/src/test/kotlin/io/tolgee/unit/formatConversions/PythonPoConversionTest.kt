package io.tolgee.unit.formatConversions

import io.tolgee.formats.po.`in`.messageConvertors.PoPythonToIcuImportMessageConvertor
import io.tolgee.formats.po.out.python.ToPythonPoMessageConvertor
import io.tolgee.testing.assert
import org.junit.jupiter.api.Test

class PythonPoConversionTest {
  @Test
  fun `it transforms`() {
    testString("Hello %(a)s")
    testString("Hello %(a)d")
    testString("Hello %(a).2f")
    testString("Hello %(a)f")
    testString("Hello %(a)e")
    testString("Hello %(a)e, hello %(v)s")
    testString("Hello %(a).50f")
    testString("Hello %(a).50f")
  }

  @Test
  fun `doesn't limit precision`() {
    convertToIcu("Hello %(a).51f")
      .assert.isEqualTo("Hello %(a).51f")
  }

  private fun testString(string: String) {
    val icuString = convertToIcu(string)
    val pythonString = ToPythonPoMessageConvertor(icuString!!, forceIsPlural = false).convert().singleResult
    pythonString.assert
      .describedAs("Input:\n${string}\nICU:\n$icuString\nPython String:\n$pythonString")
      .isEqualTo(string)
  }

  private fun convertToIcu(string: String) = PoPythonToIcuImportMessageConvertor().convert(string, "en").message
}
