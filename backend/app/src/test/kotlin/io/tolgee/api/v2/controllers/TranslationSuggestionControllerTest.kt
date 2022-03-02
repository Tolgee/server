package io.tolgee.api.v2.controllers

import com.amazonaws.services.translate.AmazonTranslate
import com.amazonaws.services.translate.model.TranslateTextResult
import com.google.cloud.translate.Translate
import com.google.cloud.translate.Translation
import io.tolgee.component.CurrentDateProvider
import io.tolgee.controllers.ProjectAuthControllerTest
import io.tolgee.development.testDataBuilder.data.SuggestionTestData
import io.tolgee.dtos.request.SuggestRequestDto
import io.tolgee.fixtures.andAssertThatJson
import io.tolgee.fixtures.andIsBadRequest
import io.tolgee.fixtures.andIsOk
import io.tolgee.fixtures.andPrettyPrint
import io.tolgee.fixtures.node
import io.tolgee.testing.annotations.ProjectJWTAuthTestMethod
import io.tolgee.testing.assertions.Assertions.assertThat
import org.apache.commons.lang3.time.DateUtils
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.web.servlet.ResultActions
import java.util.*
import kotlin.system.measureTimeMillis

class TranslationSuggestionControllerTest : ProjectAuthControllerTest("/v2/projects/") {
  lateinit var testData: SuggestionTestData

  @Autowired
  @MockBean
  lateinit var googleTranslate: Translate

  @Autowired
  @MockBean
  lateinit var amazonTranslate: AmazonTranslate

  @Autowired
  @MockBean
  lateinit var currentDateProvider: CurrentDateProvider

  @BeforeEach
  fun setup() {
    initTestData()
    initProperties()
    initMocks()
  }

  private fun initMocks() {
    mockCurrentDate { Date() }

    val googleTranslationMock = mock() as Translation
    val awsTranslateTextResult = mock() as TranslateTextResult

    whenever(
      googleTranslate.translate(
        any() as String,
        any() as Translate.TranslateOption,
        any() as Translate.TranslateOption
      )
    ).thenReturn(googleTranslationMock)

    whenever(googleTranslationMock.translatedText).thenReturn("Translated with Google")

    whenever(amazonTranslate.translateText(any())).thenReturn(awsTranslateTextResult)

    whenever(awsTranslateTextResult.translatedText).thenReturn("Translated with Amazon")
  }

  private fun initTestData() {
    testData = SuggestionTestData()
    testDataService.saveTestData(testData.root)
    projectSupplier = { testData.projectBuilder.self }
    userAccount = testData.user
  }

  private fun initProperties() {
    machineTranslationProperties.freeCreditsAmount = 1000
    awsMachineTranslationProperties.accessKey = "dummy"
    awsMachineTranslationProperties.defaultEnabled = false
    awsMachineTranslationProperties.secretKey = "dummy"
    googleMachineTranslationProperties.apiKey = "dummy"
    googleMachineTranslationProperties.defaultEnabled = true
  }

  @Test
  @ProjectJWTAuthTestMethod
  fun `it suggests from TM with keyId`() {
    performAuthPost(
      "/v2/projects/${project.id}/suggest/translation-memory",
      SuggestRequestDto(keyId = testData.thisIsBeautifulKey.id, targetLanguageId = testData.germanLanguage.id)
    ).andIsOk.andPrettyPrint.andAssertThatJson {
      node("_embedded.translationMemoryItems") {
        node("[0]") {
          node("targetText").isEqualTo("Das ist schön")
          node("baseText").isEqualTo("This is beautiful")
          node("keyName").isEqualTo("key 2")
          node("similarity").isEqualTo("0.6296296")
        }
      }
      node("page.totalElements").isEqualTo(1)
    }
  }

  @Test
  @ProjectJWTAuthTestMethod
  fun `it suggests from TM with baseText`() {
    performAuthPost(
      "/v2/projects/${project.id}/suggest/translation-memory",
      SuggestRequestDto(baseText = "This is beautiful", targetLanguageId = testData.germanLanguage.id)
    ).andIsOk.andPrettyPrint.andAssertThatJson {
      node("_embedded.translationMemoryItems") {
        node("[0]") {
          node("targetText").isEqualTo("Das ist schön")
          node("baseText").isEqualTo("This is beautiful")
          node("keyName").isEqualTo("key 2")
          node("similarity").isEqualTo("1.0")
        }
      }
      node("page.totalElements").isEqualTo(3)
    }
  }

  @Test
  @ProjectJWTAuthTestMethod
  fun `it suggests from TM fast enough`() {
    testData.generateLotOfData()
    testDataService.saveTestData(testData.root)
    val time = measureTimeMillis {
      performAuthPost(
        "/v2/projects/${project.id}/suggest/translation-memory",
        SuggestRequestDto(keyId = testData.beautifulKey.id, targetLanguageId = testData.germanLanguage.id)
      ).andIsOk
    }
    assertThat(time).isLessThan(1500)
  }

  @Test
  @ProjectJWTAuthTestMethod
  fun `it suggests machine translations with keyId`() {
    performAuthPost(
      "/v2/projects/${project.id}/suggest/machine-translations",
      SuggestRequestDto(keyId = testData.beautifulKey.id, targetLanguageId = testData.germanLanguage.id)
    ).andIsOk.andPrettyPrint.andAssertThatJson {
      node("machineTranslations") {
        node("GOOGLE").isEqualTo("Translated with Google")
      }
      node("translationCreditsBalanceBefore").isEqualTo(1000)
      node("translationCreditsBalanceAfter").isEqualTo(1000 - "Beautiful".length * 100)
    }
  }

  @Test
  @ProjectJWTAuthTestMethod
  fun `it suggests machine translations with baseText`() {
    performAuthPost(
      "/v2/projects/${project.id}/suggest/machine-translations",
      SuggestRequestDto(baseText = "Yupee", targetLanguageId = testData.germanLanguage.id)
    ).andIsOk.andPrettyPrint.andAssertThatJson {
      node("machineTranslations") {
        node("GOOGLE").isEqualTo("Translated with Google")
      }
    }
    verify(googleTranslate).translate(eq("Yupee"), any(), any())
  }

  @Test
  @ProjectJWTAuthTestMethod
  fun `it suggests using just enabled services (AWS)`() {
    testData.enableAWS()
    testDataService.saveTestData(testData.root)
    performMtRequest().andIsOk.andPrettyPrint.andAssertThatJson {
      node("machineTranslations").isEqualTo(
        """
        {
          "AWS": "Translated with Amazon"
        }
      """
      )
    }
  }

  @Test
  @ProjectJWTAuthTestMethod
  fun `it suggests using just enabled services (Google, AWS)`() {
    machineTranslationProperties.freeCreditsAmount = 2000
    testData.enableBoth()
    testDataService.saveTestData(testData.root)

    performMtRequest().andIsOk.andPrettyPrint.andAssertThatJson {
      node("machineTranslations") {
        node("AWS").isEqualTo("Translated with Amazon")
        node("GOOGLE").isEqualTo("Translated with Google")
      }
      node("translationCreditsBalanceAfter").isEqualTo(200)
    }
  }

  @Test
  @ProjectJWTAuthTestMethod
  fun `it consumes and refills bucket`() {
    val expectedCreditTaken = "Beautiful".length * 100
    testMtCreditConsumption(expectedCreditTaken)

    mockCurrentDate { DateUtils.addMonths(Date(), 1) }
    testMtCreditConsumption(expectedCreditTaken)

    mockCurrentDate { DateUtils.addMonths(Date(), 2) }
    testMtCreditConsumption(expectedCreditTaken)
  }

  private fun testMtCreditConsumption(expectedCreditTaken: Int) {
    performMtRequestAndExpectAfterBalance(1000 - expectedCreditTaken)
    performMtRequestAndExpecBadRequest()
  }

  private fun mockCurrentDate(dateProvider: () -> Date) {
    whenever(currentDateProvider.getDate()).thenAnswer { dateProvider() }
  }

  private fun performMtRequestAndExpectAfterBalance(balance: Int) {
    performMtRequest().andIsOk.andAssertThatJson {
      node("translationCreditsBalanceAfter").isEqualTo(balance)
    }
  }

  private fun performMtRequestAndExpecBadRequest() {
    performMtRequest().andIsBadRequest
  }

  private fun performMtRequest(): ResultActions {
    return performAuthPost(
      "/v2/projects/${project.id}/suggest/machine-translations",
      SuggestRequestDto(keyId = testData.beautifulKey.id, targetLanguageId = testData.germanLanguage.id)
    )
  }
}
