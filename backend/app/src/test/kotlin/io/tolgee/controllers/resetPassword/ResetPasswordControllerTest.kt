package io.tolgee.controllers.resetPassword

import com.posthog.java.PostHog
import io.tolgee.development.testDataBuilder.data.BaseTestData
import io.tolgee.dtos.request.auth.ResetPasswordRequest
import io.tolgee.fixtures.*
import io.tolgee.testing.AbstractControllerTest
import io.tolgee.testing.assert
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.times
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.mock.mockito.MockBean

@AutoConfigureMockMvc
class ResetPasswordControllerTest :
  AbstractControllerTest() {
  private var frontEndUrlBefore: String? = null

  @BeforeEach
  fun setup() {
    emailTestUtil.initMocks()
    frontEndUrlBefore = tolgeeProperties.frontEndUrl
    testData = BaseTestData()
    saveTestData()
  }

  @AfterEach
  fun tearDown() {
    tolgeeProperties.frontEndUrl = frontEndUrlBefore
  }

  private lateinit var testData: BaseTestData

  @Autowired
  private lateinit var emailTestUtil: EmailTestUtil

  @MockBean
  @Autowired
  lateinit var postHog: PostHog

  @Test
  fun `email contains correct callback url with frontend url provided`() {
    tolgeeProperties.frontEndUrl = "http://dummy-url.com/"
    executePasswordChangeRequest()
    emailTestUtil.firstMessageContent.assert.contains("http://dummy-url.com/reset_password/")
    // We don't want double slashes
    emailTestUtil.firstMessageContent.assert.doesNotContain("reset_password//")
  }

  @Test
  fun `email contains correct callback url without frontend url provided`() {
    executePasswordChangeRequest()
    emailTestUtil.firstMessageContent.assert.contains("https://hello.com/aa/")
    // We don't want double slashes
    emailTestUtil.firstMessageContent.assert.doesNotContain("aa//")
  }

  private fun executePasswordChangeRequest() {
    val dto =
      ResetPasswordRequest(
        email = testData.user.username,
        callbackUrl = "https://hello.com/aa/",
      )
    performPost("/api/public/reset_password_request", dto).andIsOk
  }

  private fun saveTestData() {
    testData.user.username = "example@example.com"
    testDataService.saveTestData(testData.root)
  }
}
