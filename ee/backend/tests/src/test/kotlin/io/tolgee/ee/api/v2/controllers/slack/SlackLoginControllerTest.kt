package io.tolgee.ee.api.v2.controllers.slack

import io.tolgee.constants.Feature
import io.tolgee.development.testDataBuilder.data.SlackNoUserConnectionTestData
import io.tolgee.development.testDataBuilder.data.SlackTestData
import io.tolgee.ee.component.PublicEnabledFeaturesProvider
import io.tolgee.ee.component.slackIntegration.SlackUserLoginUrlProvider
import io.tolgee.ee.service.slackIntegration.SlackUserConnectionService
import io.tolgee.fixtures.andIsBadRequest
import io.tolgee.fixtures.andIsOk
import io.tolgee.testing.AuthorizedControllerTest
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThatCode
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class SlackLoginControllerTest : AuthorizedControllerTest() {
  @Autowired
  lateinit var slackUserConnectionService: SlackUserConnectionService

  @Autowired
  lateinit var slackUserLoginUrlProvider: SlackUserLoginUrlProvider

  @Autowired
  private lateinit var enabledFeaturesProvider: PublicEnabledFeaturesProvider

  @BeforeAll
  fun setUp() {
    tolgeeProperties.slack.token = "token"
    enabledFeaturesProvider.forceEnabled = setOf(Feature.SLACK_INTEGRATION)
  }

  @Test
  fun `user logs in`() {
    val testData = SlackNoUserConnectionTestData()
    testDataService.saveTestData(testData.root)

    slackUserLoginUrlProvider.encryptData("ChannelTest", "TEST1", testData.slackWorkspace.id).let {
      performAuthPost("/v2/slack/user-login?data=$it", null).andIsOk
    }

    Assertions.assertThat(slackUserConnectionService.findBySlackId("TEST1")).isNotNull()
  }

  @Test
  fun `should not allow duplicate SlackUserConnection for same Tolgee account and workspace`() {
    val testData = SlackTestData()
    testDataService.saveTestData(testData.root)

    slackUserLoginUrlProvider.encryptData("ChannelTest", "TEST1", testData.slackWorkspace.id).let {
      performAuthPost("/v2/slack/user-login?data=$it", null).andIsBadRequest
    }

    assertThatCode {
      slackUserConnectionService.findBySlackId("TEST1")
    }.doesNotThrowAnyException()
  }

  @Test
  fun `logs in for same Tolgee acc and different workspace`() {
    val testData = SlackTestData()
    testDataService.saveTestData(testData.root)

    slackUserLoginUrlProvider.encryptData("ChannelTest", "slackUserId", testData.slackWorkspace2.id).let {
      performAuthPost("/v2/slack/user-login?data=$it", null).andIsOk
    }

    assertThatCode {
      slackUserConnectionService.findBySlackId("slackUserId")
    }.isNotNull()
  }

  @Test
  fun `should not allow duplicate SlackUserConnection for same Tolgee account and workspace and different Slack acc`() {
    val testData = SlackTestData()
    testDataService.saveTestData(testData.root)

    slackUserLoginUrlProvider.encryptData("ChannelTest", "slackUserId322", testData.slackWorkspace.id).let {
      performAuthPost("/v2/slack/user-login?data=$it", null).andIsBadRequest
    }

    assertThatCode {
      slackUserConnectionService.findBySlackId(testData.slackUserConnection.slackTeamId!!)
    }.doesNotThrowAnyException()
  }
}
