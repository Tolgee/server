package io.tolgee.testing

import io.tolgee.model.notifications.Notification
import io.tolgee.repository.notification.NotificationRepository
import org.springframework.data.domain.Sort
import org.springframework.data.domain.Sort.Direction.DESC
import org.springframework.stereotype.Component

@Component
class NotificationUtil(
  private val notificationRepository: NotificationRepository,
) {
  fun newestNotification(): Notification =
    notificationRepository.findAll(Sort.by(DESC, "id")).first()
}
