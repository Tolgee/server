package io.tolgee.api.v2.controllers

import io.tolgee.development.testDataBuilder.data.NotificationsTestData
import io.tolgee.fixtures.andAssertThatJson
import io.tolgee.fixtures.andIsOk
import io.tolgee.repository.NotificationRepository
import io.tolgee.testing.AuthorizedControllerTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class NotificationControllerTest : AuthorizedControllerTest() {
  @Autowired
  lateinit var notificationRepository: NotificationRepository

  @Test
  fun `gets notifications from newest`() {
    val testData = NotificationsTestData()

    (101L..103).forEach { taskNumber ->
      testData.generateNotificationWithTask(taskNumber)
    }

    testDataService.saveTestData(testData.root)
    loginAsUser(testData.user.username)

    performAuthGet("/v2/notifications").andAssertThatJson {
      node("_embedded.notificationModelList[0].linkedTask.name").isEqualTo("Notification task 103")
      node("_embedded.notificationModelList[1].linkedTask.name").isEqualTo("Notification task 102")
      node("_embedded.notificationModelList[2].linkedTask.name").isEqualTo("Notification task 101")
    }
  }

  @Test
  fun `marks notifications as seen`() {
    val testData = NotificationsTestData()
    val currentUserNotification1 = testData.generateNotificationWithTask()
    val currentUserNotification2 = testData.generateNotificationWithTask()
    val differentUserNotification =
      testData.generateNotificationWithTask().apply {
        user = testData.root.addUserAccountWithoutOrganization { username = "Different User" }.self
      }

    testDataService.saveTestData(testData.root)
    loginAsUser(testData.user.username)

    performAuthPut(
      "/v2/notifications-mark-seen",
      listOf(currentUserNotification1.id, currentUserNotification2.id, differentUserNotification.id),
    ).andIsOk

    val notifications = notificationRepository.findAll()

    assertThat(notifications.find { it.id == currentUserNotification1.id }?.seen).isTrue()
    assertThat(notifications.find { it.id == currentUserNotification2.id }?.seen).isTrue()
    assertThat(notifications.find { it.id == differentUserNotification.id }?.seen).isFalse()
  }
}
