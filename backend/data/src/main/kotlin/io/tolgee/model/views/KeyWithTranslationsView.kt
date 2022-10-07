package io.tolgee.model.views

import io.tolgee.constants.MtServiceType
import io.tolgee.model.Language
import io.tolgee.model.Screenshot
import io.tolgee.model.enums.TranslationState
import io.tolgee.model.key.Tag

data class KeyWithTranslationsView(
  val keyId: Long,
  val keyName: String,
  val namespace: String?,
  val screenshotCount: Long,
  val translations: MutableMap<String, TranslationView> = mutableMapOf(),
) {
  lateinit var keyTags: List<Tag>
  var screenshots: Collection<Screenshot>? = null

  companion object {
    fun of(queryData: Array<Any?>, languages: List<Language>): KeyWithTranslationsView {
      val data = mutableListOf(*queryData)
      val result = KeyWithTranslationsView(
        keyId = data.removeFirst() as Long,
        keyName = data.removeFirst() as String,
        namespace = data.removeFirst() as String?,
        screenshotCount = data.removeFirst() as Long
      )

      (0 until data.size step 7).forEach { i ->
        val language = languages[i / 7].tag

        val id = data[i] as Long?
        if (id != null) {
          result.translations[language] = TranslationView(
            id = id,
            text = data[i + 1] as String?,
            state = (data[i + 2] ?: TranslationState.TRANSLATED) as TranslationState,
            auto = data[i + 3] as Boolean,
            mtProvider = data[i + 4] as MtServiceType?,
            commentCount = (data[i + 5]) as Long,
            unresolvedCommentCount = (data[i + 6]) as Long
          )
        }
      }
      return result
    }
  }
}
