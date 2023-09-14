package io.tolgee.dtos.cacheable.automations

import io.tolgee.model.automations.AutomationActionType

class AutomationActionDto(
  var id: Long,
  var type: AutomationActionType = AutomationActionType.CDN_PUBLISH
)
