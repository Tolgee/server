package io.tolgee.model.automations

import io.tolgee.activity.data.ActivityType
import io.tolgee.model.StandardAuditModel
import javax.persistence.Entity
import javax.persistence.EnumType.STRING
import javax.persistence.Enumerated
import javax.persistence.FetchType
import javax.persistence.ManyToOne

@Entity
class AutomationTrigger(
  @ManyToOne(fetch = FetchType.LAZY)
  var automation: Automation,
) : StandardAuditModel() {
  @Enumerated(STRING)
  var type: AutomationTriggerType = AutomationTriggerType.TRANSLATION_DATA_MODIFICATION

  /**
   * only when type is ACTIVITY, if type is ACTIVITY and this is null, it means all activity types
   */
  @Enumerated(STRING)
  var activityType: ActivityType? = null

  var debounceDurationInMs: Long? = null
}
