package io.tolgee.service.query_builders

import io.tolgee.testing.assertions.Assertions.assertThat
import io.tolgee.fixtures.node
import io.tolgee.model.enums.TranslationState
import io.tolgee.model.views.KeyWithTranslationsView
import io.tolgee.model.views.TranslationView
import net.javacrumbs.jsonunit.assertj.assertThatJson
import org.springframework.data.domain.Sort
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test
import java.util.*

class CursorUtilUnitTest {

  lateinit var cursor: String

  @BeforeMethod
  fun setup() {
    val item = KeyWithTranslationsView(
      keyId = 1,
      keyName = "Super key",
      screenshotCount = 1,
      translations = mutableMapOf(
        "en" to TranslationView(
          1,
          "Super key translated \uD83C\uDF8C",
          TranslationState.TRANSLATED,
          0
        )
      )
    )
    cursor = CursorUtil.getCursor(
      item,
      Sort.by(
        Sort.Order.asc("translations.en.text"),
        Sort.Order.desc("keyName")
      )
    )
  }

  @Test
  fun `generates cursor`() {
    val decoded = String(Base64.getDecoder().decode(cursor))
    assertThatJson(decoded).apply {
      node("keyName").isEqualTo("{\"direction\":\"DESC\",\"value\":\"Super key\"}")
      node("translations\\.en\\.text") {
        node("direction").isEqualTo("ASC")
        node("value").isEqualTo("Super key translated 🎌")
      }
      node("keyId") {
        node("direction").isEqualTo("ASC")
        node("value").isString.isEqualTo("1")
      }
    }
  }

  @Test
  fun `parses cursor`() {
    val parsed = CursorUtil.parseCursor(cursor)
    assertThat(parsed["keyId"]?.direction).isEqualTo(Sort.Direction.ASC)
    assertThat(parsed["keyId"]?.value).isEqualTo("1")
    assertThat(parsed.entries).hasSize(3)
  }
}
