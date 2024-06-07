package io.tolgee.ee.repository.slackIntegration

import io.tolgee.model.slackIntegration.SlackConfig
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface SlackConfigRepository : JpaRepository<SlackConfig, Long> {
  fun findByProjectIdAndChannelId(
    id: Long,
    channelId: String,
  ): SlackConfig?

  fun getAllByChannelId(channelId: String): List<SlackConfig>
}
