package io.tolgee.dtos

import javax.validation.constraints.NotEmpty

class BigMetaItemDto {
  var namespace: String? = null

  @field:NotEmpty
  var keyName: String = ""
}
