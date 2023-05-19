package io.tolgee.api.v2.controllers

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.tolgee.ProjectAuthControllerTest
import io.tolgee.development.testDataBuilder.data.LanguagePermissionsTestData
import io.tolgee.development.testDataBuilder.data.NamespacesTestData
import io.tolgee.development.testDataBuilder.data.TranslationsTestData
import io.tolgee.fixtures.andAssertThatJson
import io.tolgee.fixtures.andGetContentAsString
import io.tolgee.fixtures.andIsOk
import io.tolgee.fixtures.andPrettyPrint
import io.tolgee.fixtures.retry
import io.tolgee.testing.ContextRecreatingTest
import io.tolgee.testing.annotations.ProjectJWTAuthTestMethod
import io.tolgee.testing.assert
import io.tolgee.testing.assertions.Assertions.assertThat
import net.javacrumbs.jsonunit.assertj.assertThatJson
import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.MvcResult
import org.springframework.transaction.annotation.Transactional
import java.io.ByteArrayInputStream
import java.util.zip.ZipInputStream
import kotlin.system.measureTimeMillis

@ContextRecreatingTest
class V2ExportControllerTest : ProjectAuthControllerTest("/v2/projects/") {
  lateinit var testData: TranslationsTestData

  @Test
  @Transactional
  @ProjectJWTAuthTestMethod
  fun `it exports to json`() {
    initBaseData()
    val parsed = performExport()

    assertThatJson(parsed["en.json"]!!) {
      node("Z key").isEqualTo("A translation")
    }
  }

  @Test
  @Transactional
  @ProjectJWTAuthTestMethod
  fun `it exports to single json`() {
    initBaseData()
    retry {
      val response = performProjectAuthGet("export?languages=en&zip=false")
        .andDo { obj: MvcResult -> obj.asyncResult }
      response.andPrettyPrint.andAssertThatJson {
        node("Z key").isEqualTo("A translation")
      }
      assertThat(response.andReturn().response.getHeaderValue("content-type"))
        .isEqualTo("application/json")
      assertThat(response.andReturn().response.getHeaderValue("content-disposition"))
        .isEqualTo("""attachment; filename="en.json"""")
    }
  }

  @Test
  @Transactional
  @ProjectJWTAuthTestMethod
  fun `it exports to single xliff`() {
    initBaseData()
    retry {
      val response = performProjectAuthGet("export?languages=en&zip=false&format=XLIFF")
        .andDo { obj: MvcResult -> obj.getAsyncResult(30000) }

      assertThat(response.andReturn().response.getHeaderValue("content-type"))
        .isEqualTo("application/x-xliff+xml")
      assertThat(response.andReturn().response.getHeaderValue("content-disposition"))
        .isEqualTo("""attachment; filename="en.xlf"""")
    }
  }

  @Test
  @Transactional
  @ProjectJWTAuthTestMethod
  fun `it filters by keyId in`() {
    testData = TranslationsTestData()
    testData.generateLotOfData(1000)
    testDataService.saveTestData(testData.root)
    prepareUserAndProject(testData)
    commitTransaction()

    val time = measureTimeMillis {
      val selectAllResult = performProjectAuthGet("translations/select-all")
        .andIsOk
        .andGetContentAsString
      val keyIds = jacksonObjectMapper().readValue<Map<String, List<Long>>>(selectAllResult)["ids"]?.take(500)
      val parsed = performExportPost(mapOf("filterKeyId" to keyIds))
      assertThatJson(parsed["en.json"]!!) {
        isObject.hasSize(499)
      }
    }

    assertThat(time).isLessThan(2000)
  }

  @Test
  @Transactional
  @ProjectJWTAuthTestMethod
  fun `the structureDelimiter works`() {
    testData = TranslationsTestData()
    testData.generateScopedData()
    testDataService.saveTestData(testData.root)
    prepareUserAndProject(testData)
    commitTransaction()

    performExport("structureDelimiter=").let { parsed ->
      assertThatJson(parsed["en.json"]!!) {
        node("hello\\.i\\.am\\.scoped").isEqualTo("yupee!")
      }
    }
    performExport("structureDelimiter=+").let { parsed ->
      assertThatJson(parsed["en.json"]!!) {
        node("hello.i.am.plus.scoped").isEqualTo("yupee!")
      }
    }
    performExport("").let { parsed ->
      assertThatJson(parsed["en.json"]!!) {
        node("hello.i.am.scoped").isEqualTo("yupee!")
      }
    }
  }

  private fun performExport(query: String = ""): Map<String, String> {
    val mvcResult = performProjectAuthGet("export?$query")
      .andIsOk
      .andDo { obj: MvcResult -> obj.asyncResult }.andReturn()
    return parseZip(mvcResult.response.contentAsByteArray)
  }

  private fun performExportPost(body: Any): Map<String, String> {
    val mvcResult = performProjectAuthPost("export", body)
      .andIsOk
      .andDo { obj: MvcResult -> obj.asyncResult }.andReturn()
    return parseZip(mvcResult.response.contentAsByteArray)
  }

  private fun parseZip(responseContent: ByteArray): Map<String, String> {
    val byteArrayInputStream = ByteArrayInputStream(responseContent)
    val zipInputStream = ZipInputStream(byteArrayInputStream)

    return zipInputStream.use {
      generateSequence {
        it.nextEntry
      }.filterNot { it.isDirectory }
        .map { it.name to zipInputStream.bufferedReader().readText() }.toMap()
    }
  }

  @Test
  @Transactional
  @ProjectJWTAuthTestMethod
  fun `it exports to json with namespaces`() {
    val namespacesTestData = NamespacesTestData()
    testDataService.saveTestData(namespacesTestData.root)
    projectSupplier = { namespacesTestData.projectBuilder.self }
    userAccount = namespacesTestData.user

    val parsed = performExport()

    assertThatJson(parsed["ns-1/en.json"]!!) {
      node("key").isEqualTo("hello")
    }
    assertThatJson(parsed["en.json"]!!) {
      node("key").isEqualTo("hello")
    }
  }

  @Test
  @Transactional
  @ProjectJWTAuthTestMethod
  fun `it exports only allowed languages`() {
    val testData = LanguagePermissionsTestData()
    testDataService.saveTestData(testData.root)
    projectSupplier = { testData.projectBuilder.self }
    userAccount = testData.viewEnOnlyUser

    val parsed = performExport()
    val files = parsed.keys
    files.assert.containsExactly("en.json")
  }

  private fun initBaseData() {
    testData = TranslationsTestData()
    testDataService.saveTestData(testData.root)
    prepareUserAndProject(testData)
    commitTransaction()
  }

  private fun prepareUserAndProject(testData: TranslationsTestData) {
    userAccount = testData.user
    projectSupplier = { testData.project }
  }
}
