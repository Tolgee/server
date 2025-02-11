package io.tolgee.service.notification

import io.tolgee.model.UserAccount
import io.tolgee.model.notifications.NotificationChannel
import io.tolgee.model.notifications.NotificationSetting
import io.tolgee.model.notifications.NotificationTypeGroup
import io.tolgee.repository.notification.NotificationSettingRepository
import org.springframework.stereotype.Service

@Service
class NotificationSettingService(
  private val notificationSettingRepository: NotificationSettingRepository,
) {
  fun getSettings(user: UserAccount): List<NotificationSetting> {
    val dbData = notificationSettingRepository.findByUserId(user.id)

    return NotificationTypeGroup.entries.flatMap { group ->
      NotificationChannel.entries.map { channel ->
        dbData.find { it.group == group && it.channel == channel } ?: defaultSettings(user, group, channel)
      }
    }
  }

  private fun defaultSettings(
    user: UserAccount,
    group: NotificationTypeGroup,
    channel: NotificationChannel,
  ) = NotificationSetting().apply {
    this.user = user
    this.group = group
    this.channel = channel
    this.enabled = true
  }
}
