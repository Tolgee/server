package io.tolgee.ee.service.slackIntegration

import io.tolgee.ee.repository.slackIntegration.SlackConfigPreferenceRepository
import io.tolgee.model.slackIntegration.EventName
import io.tolgee.model.slackIntegration.SlackConfig
import io.tolgee.model.slackIntegration.SlackConfigPreference
import org.springframework.stereotype.Service

@Service
class SlackConfigPreferenceService(
  private val slackConfigPreferenceRepository: SlackConfigPreferenceRepository,
) {
  fun create(
    slackConfig: SlackConfig,
    langTag: String,
    events: MutableSet<EventName>,
  ): SlackConfigPreference {
    val preference =
      SlackConfigPreference(slackConfig, langTag).apply {
        this.events =
          if (events.isEmpty()) {
            mutableSetOf(EventName.ALL)
          } else {
            events
          }
      }
    return slackConfigPreferenceRepository.save(preference)
  }

  fun update(
    slackConfigPreference: SlackConfigPreference,
    events: MutableSet<EventName>,
  ): SlackConfigPreference {
    if (events.isNotEmpty()) {
      slackConfigPreference.events = events
    }
    return slackConfigPreferenceRepository.save(slackConfigPreference)
  }

  fun delete(slackConfigPreference: SlackConfigPreference) {
    slackConfigPreferenceRepository.delete(slackConfigPreference)
  }
}
