package io.tolgee.formats.android.out

import io.tolgee.formats.MessageConvertorFactory
import io.tolgee.formats.PossiblePluralConversionResult

class IcuToJavaMessageConvertor(
  private val message: String,
  private val forceIsPlural: Boolean? = null,
  private val isProjectIcuPlaceholdersEnabled: Boolean,
) {
  fun convert(): PossiblePluralConversionResult {
    return MessageConvertorFactory(message, forceIsPlural, isProjectIcuPlaceholdersEnabled) {
      JavaFromIcuParamConvertor()
    }.create().convert()
  }
}
