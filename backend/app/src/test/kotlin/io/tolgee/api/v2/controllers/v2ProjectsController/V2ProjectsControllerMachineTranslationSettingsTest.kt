package io.tolgee.api.v2.controllers.v2ProjectsController

import io.tolgee.constants.MachineTranslationServiceType
import io.tolgee.controllers.ProjectAuthControllerTest
import io.tolgee.development.testDataBuilder.data.MachineTranslationSettingsTestData
import io.tolgee.dtos.request.MachineTranslationLanguagePropsDto
import io.tolgee.dtos.request.SetMachineTranslationSettingsDto
import io.tolgee.fixtures.andAssertThatJson
import io.tolgee.fixtures.andPrettyPrint
import io.tolgee.fixtures.isValidId
import io.tolgee.fixtures.node
import io.tolgee.testing.annotations.ProjectJWTAuthTestMethod
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test

@SpringBootTest
@AutoConfigureMockMvc
class V2ProjectsControllerMachineTranslationSettingsTest : ProjectAuthControllerTest() {

  lateinit var testData: MachineTranslationSettingsTestData

  @BeforeMethod
  fun setup() {
    testData = MachineTranslationSettingsTestData()
    testDataService.saveTestData(testData.root)
    awsMachineTranslationProperties.accessKey = "dummy"
    awsMachineTranslationProperties.secretKey = "dummy"
    googleMachineTranslationProperties.apiKey = "dummy"
    projectSupplier = { testData.projectBuilder.self }
    userAccount = testData.user
  }

  @Test
  @ProjectJWTAuthTestMethod
  fun `it sets the configuration`() {
    performAuthPut(
      "/v2/projects/${project.id}/machine-translation-service-settings",
      SetMachineTranslationSettingsDto(
        listOf(MachineTranslationLanguagePropsDto(
          targetLanguageId = testData.englishLanguage.id,
          primaryService = MachineTranslationServiceType.GOOGLE,
          enabledServices = setOf(MachineTranslationServiceType.AWS, MachineTranslationServiceType.GOOGLE)
        ))
      )).andPrettyPrint.andAssertThatJson {
      node("_embedded.languageConfigs") {
        node("[0]") {
          node("targetLanguageId").isNull()
          node("primaryService").isEqualTo("GOOGLE")
          node("enabledServices").isArray.isEqualTo("""[ "GOOGLE", "AWS" ]""")
        }
        node("[1]") {
          node("targetLanguageId").isValidId
          node("targetLanguageTag").isEqualTo("en")
          node("targetLanguageName").isEqualTo("English")
          node("primaryService").isEqualTo("GOOGLE")
          node("enabledServices").isArray.isEqualTo("""[ "GOOGLE", "AWS" ]""")
        }
      }
    }
  }

  @Test
  @ProjectJWTAuthTestMethod
  fun `it returns the configuration`() {
    performAuthGet(
      "/v2/projects/${project.id}/machine-translation-service-settings").andPrettyPrint.andAssertThatJson {
      node("_embedded.languageConfigs") {
        node("[0]") {
          node("targetLanguageId").isNull()
          node("primaryService").isEqualTo("GOOGLE")
          node("enabledServices").isArray.isEqualTo("""[ "GOOGLE", "AWS" ]""")
        }
        node("[1]") {
          node("targetLanguageId").isValidId
          node("targetLanguageTag").isEqualTo("de")
          node("targetLanguageName").isEqualTo("German")
          node("primaryService").isEqualTo("AWS")
          node("enabledServices").isArray.isEqualTo("""[ "AWS" ]""")
        }
      }
    }
  }
}
