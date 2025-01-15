package io.tolgee.websocket

import io.tolgee.ProjectAuthControllerTest
import io.tolgee.development.testDataBuilder.data.BaseTestData
import io.tolgee.fixtures.andIsOk
import io.tolgee.fixtures.isValidId
import io.tolgee.fixtures.node
import io.tolgee.fixtures.waitFor
import io.tolgee.model.Notification
import io.tolgee.model.UserAccount
import io.tolgee.model.key.Key
import io.tolgee.model.translation.Translation
import io.tolgee.service.notification.NotificationService
import io.tolgee.testing.WebsocketTest
import io.tolgee.testing.annotations.ProjectJWTAuthTestMethod
import io.tolgee.testing.assert
import net.javacrumbs.jsonunit.assertj.assertThatJson
import org.junit.jupiter.api.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.web.server.LocalServerPort

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@WebsocketTest
abstract class AbstractWebsocketTest : ProjectAuthControllerTest("/v2/projects/") {
  lateinit var testData: BaseTestData
  lateinit var translation: Translation
  lateinit var key: Key
  lateinit var notPermittedUser: UserAccount
  lateinit var helper: WebsocketTestHelper

  @Autowired
  lateinit var notificationService: NotificationService

  @LocalServerPort
  private val port: Int? = null

  @BeforeEach
  fun beforeEach() {
    prepareTestData()
    helper =
      WebsocketTestHelper(
        port,
        jwtService.emitToken(testData.user.id),
        testData.projectBuilder.self.id,
        testData.user.id,
      )
  }

  @AfterEach
  fun after() {
    helper.stop()
  }

  @Test
  @ProjectJWTAuthTestMethod
  fun `notifies on key modification`() {
    helper.listenForTranslationDataModified()
    helper.assertNotified(
      {
        performProjectAuthPut("keys/${key.id}", mapOf("name" to "name edited"))
      },
    ) {
      assertThatJson(it.poll()).apply {
        node("actor") {
          node("data") {
            node("username").isEqualTo("test_username")
          }
        }
        node("data") {
          node("keys") {
            isArray
            node("[0]") {
              node("id").isValidId
              node("modifications") {
                node("name") {
                  node("old").isEqualTo("key")
                  node("new").isEqualTo("name edited")
                }
              }
              node("changeType").isEqualTo("MOD")
            }
          }
        }
        node("sourceActivity").isEqualTo("KEY_NAME_EDIT")
        node("dataCollapsed").isEqualTo(false)
      }
    }
  }

  @Test
  @ProjectJWTAuthTestMethod
  fun `notifies on key deletion`() {
    helper.listenForTranslationDataModified()
    helper.assertNotified(
      {
        performProjectAuthDelete("keys/${key.id}")
      },
    ) {
      assertThatJson(it.poll()).apply {
        node("data") {
          node("keys") {
            isArray
            node("[0]") {
              node("id").isValidId
              node("modifications") {
                node("name") {
                  node("old").isEqualTo("key")
                  node("new").isEqualTo(null)
                }
              }
              node("changeType").isEqualTo("DEL")
            }
          }
        }
      }
    }
  }

  @Test
  @ProjectJWTAuthTestMethod
  fun `notifies on key creation`() {
    helper.listenForTranslationDataModified()
    helper.assertNotified(
      {
        performProjectAuthPost("keys", mapOf("name" to "new key"))
      },
    ) {
      assertThatJson(it.poll()).apply {
        node("data") {
          node("keys") {
            isArray
            node("[0]") {
              node("id").isValidId
              node("modifications") {
                node("name") {
                  node("old").isEqualTo(null)
                  node("new").isEqualTo("new key")
                }
              }
              node("changeType").isEqualTo("ADD")
            }
          }
        }
      }
    }
  }

  @Test
  @ProjectJWTAuthTestMethod
  fun `notifies on translation modification`() {
    helper.listenForTranslationDataModified()
    helper.assertNotified(
      {
        performProjectAuthPut(
          "translations",
          mapOf(
            "key" to key.name,
            "translations" to mapOf("en" to "haha"),
          ),
        ).andIsOk
      },
    ) {
      assertThatJson(it.poll()).apply {
        node("data") {
          node("translations") {
            isArray
            node("[0]") {
              node("id").isValidId
              node("modifications") {
                node("text") {
                  node("old").isEqualTo("translation")
                  node("new").isEqualTo("haha")
                }
              }
              node("relations") {
                node("key") {
                  node("data") {
                    node("name").isEqualTo("key")
                  }
                }
                node("language") {
                  node("data") {
                    node("name").isEqualTo("English")
                  }
                }
              }
              node("changeType").isEqualTo("MOD")
            }
          }
        }
      }
    }
  }

  /**
   * The request is made by permitted user, but user without permission tries to listen, so they shell
   * not be notified
   */
  @Test
  @ProjectJWTAuthTestMethod
  fun `doesn't subscribe without permissions`() {
    helper.listenForTranslationDataModified()
    val notPermittedSubscriptionHelper =
      WebsocketTestHelper(
        port,
        jwtService.emitToken(notPermittedUser.id),
        testData.projectBuilder.self.id,
        notPermittedUser.id,
      )
    notPermittedSubscriptionHelper.listenForTranslationDataModified()
    performProjectAuthPut(
      "translations",
      mapOf(
        "key" to key.name,
        "translations" to mapOf("en" to "haha"),
      ),
    ).andIsOk

    assertPermittedUserReceivedMessage()
    notPermittedSubscriptionHelper.receivedMessages.assert.isEmpty()
  }

  @Test
  @ProjectJWTAuthTestMethod
  fun `doesn't subscribe as another user`() {
    helper.listenForNotificationsChanged()
    val notPermittedSubscriptionHelper =
      WebsocketTestHelper(
        port,
        jwtService.emitToken(notPermittedUser.id),
        testData.projectBuilder.self.id,
        testData.user.id, // notPermittedUser trying to spy on other user's websocket
      )
    notPermittedSubscriptionHelper.listenForNotificationsChanged()
    saveNotificationForCurrentUser()

    assertPermittedUserReceivedMessage()
    notPermittedSubscriptionHelper.receivedMessages.assert.isEmpty()
  }

  private fun assertPermittedUserReceivedMessage() {
    waitFor { helper.receivedMessages.isNotEmpty() }
  }

  private fun saveNotificationForCurrentUser() {
    executeInNewTransaction {
      notificationService.save(Notification().apply { user = testData.user })
    }
  }

  private fun prepareTestData() {
    testData = BaseTestData()
    testData.root.addUserAccount {
      username = "franta"
      notPermittedUser = this
    }
    testData.projectBuilder.apply {
      addKey {
        name = "key"
        key = this
      }.build {
        addTranslation {
          language = testData.englishLanguage
          text = "translation"
          translation = this
        }
      }
    }
    testDataService.saveTestData(testData.root)
    userAccount = testData.user
    projectSupplier = { testData.projectBuilder.self }
  }
}
