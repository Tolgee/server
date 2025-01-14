package io.tolgee.service.notification

import io.tolgee.component.CurrentDateProvider
import io.tolgee.model.Notification
import io.tolgee.repository.NotificationRepository
import io.tolgee.websocket.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class NotificationService(
  private val notificationRepository: NotificationRepository,
  private val websocketEventPublisher: WebsocketEventPublisher,
  private val currentDateProvider: CurrentDateProvider,
) {
  fun getNotifications(
    userId: Long,
    pageable: Pageable,
  ): Page<Notification> {
    return notificationRepository.fetchNotificationsByUserId(userId, pageable)
  }

  fun getCountOfUnseenNotifications(
    userId: Long,
  ): Int {
    return notificationRepository.getUnseenCountByUserId(userId)
  }

  fun save(notification: Notification) {
    notificationRepository.save(notification)
    websocketEventPublisher(
      "/users/${notification.user.id}/${WebsocketEventType.NOTIFICATIONS_CHANGED.typeName}",
      WebsocketEvent(
        actor = ActorInfo(
          type = ActorType.USER,
          data = null,
        ),
        data = null,
        sourceActivity = null,
        activityId = null,
        dataCollapsed = false,
        timestamp = currentDateProvider.date.time,
      ),
    )
  }
}
