/*
 * Copyright (c) 2020. Tolgee
 */

package io.tolgee.controllers.screenshot

import io.tolgee.dtos.request.key.CreateKeyDto
import io.tolgee.dtos.request.screenshot.GetScreenshotsByKeyDto
import io.tolgee.dtos.response.ScreenshotDTO
import io.tolgee.fixtures.AuthorizedRequestFactory.addToken
import io.tolgee.fixtures.andIsOk
import io.tolgee.fixtures.generateUniqueString
import io.tolgee.testing.assertions.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.io.File
import java.util.stream.Collectors

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ScreenshotControllerTest : AbstractScreenshotControllerTest() {

  @Test
  fun uploadScreenshot() {
    val base = dbPopulator.createBase(generateUniqueString())
    val project = base.project

    val key = keyService.create(project, CreateKeyDto("test"))

    val responseBody: ScreenshotDTO = performStoreScreenshot(project, key)

    val screenshots = screenshotService.findAll(key = key)
    assertThat(screenshots).hasSize(1)
    val file = File(tolgeeProperties.fileStorage.fsDataPath + "/screenshots/" + screenshots[0].filename)
    assertThat(file).exists()
    assertThat(responseBody.filename).isEqualTo(screenshots[0].filename)
  }

  @Test
  fun uploadMultipleScreenshots() {
    val base = dbPopulator.createBase(generateUniqueString())
    val project = base.project
    val key = keyService.create(project, CreateKeyDto("test"))
    repeat((1..5).count()) {
      performStoreScreenshot(project, key)
    }
    assertThat(screenshotService.findAll(key = key)).hasSize(5)
  }

  @Test
  fun findAll() {
    val base = dbPopulator.createBase(generateUniqueString())
    val project = base.project
    val key = keyService.create(project, CreateKeyDto("test"))
    val key2 = keyService.create(project, CreateKeyDto("test_2"))

    screenshotService.store(screenshotFile, key)
    screenshotService.store(screenshotFile, key)
    screenshotService.store(screenshotFile, key2)

    var result: List<ScreenshotDTO> = performAuthPost(
      "/api/project/${project.id}/screenshots/get",
      GetScreenshotsByKeyDto(key.name)
    ).andExpect(status().isOk)
      .andReturn().parseResponseTo()

    assertThat(result).hasSize(2)

    val file = File(tolgeeProperties.fileStorage.fsDataPath + "/screenshots/" + result[0].filename)
    assertThat(file).exists()

    result = performAuthPost(
      "/api/project/${project.id}/screenshots/get",
      GetScreenshotsByKeyDto(key2.name)
    ).andExpect(status().isOk)
      .andReturn().parseResponseTo()

    assertThat(result).hasSize(1)
  }

  @Test
  fun getScreenshotFile() {
    val base = dbPopulator.createBase(generateUniqueString())
    val project = base.project
    val key = keyService.create(project, CreateKeyDto("test"))
    val screenshot = screenshotService.store(screenshotFile, key)

    val file = File(tolgeeProperties.fileStorage.fsDataPath + "/screenshots/" + screenshot.filename)
    val result = performAuthGet("/screenshots/${screenshot.filename}")
      .andExpect(status().isOk)
      .andExpect(
        header().string("Cache-Control", "max-age=365, must-revalidate, no-transform")
      )
      .andReturn()

    assertThat(result.response.contentAsByteArray).isEqualTo(file.readBytes())
  }

  @Test
  fun delete() {
    val base = dbPopulator.createBase(generateUniqueString())
    val project = base.project
    val key = keyService.create(project, CreateKeyDto("test"))

    val list = (1..20).map {
      screenshotService.store(screenshotFile, key)
    }.toCollection(mutableListOf())

    val idsToDelete = list.stream().limit(10).map { it.id.toString() }.collect(Collectors.joining(","))

    performAuthDelete("/api/project/screenshots/$idsToDelete", null).andIsOk

    val rest = screenshotService.findAll(key)
    assertThat(rest).isEqualTo(list.stream().skip(10).collect(Collectors.toList()))
  }

  @Test
  fun uploadValidationNoImage() {
    val base = dbPopulator.createBase(generateUniqueString())
    val project = base.project
    val key = keyService.create(project, CreateKeyDto("test"))
    loginAsUser("admin")
    val response = mvc.perform(
      addToken(
        multipart("/api/project/${project.id}/screenshots")
          .file(
            MockMultipartFile(
              "screenshot", "originalShot.png", "not_valid",
              "test".toByteArray()
            )
          )
          .param("key", key.name)
      )
    )
      .andExpect(status().isBadRequest).andReturn()

    assertThat(response).error().isCustomValidation.hasMessage("file_not_image")
  }

  @Test
  fun uploadValidationBlankKey() {
    val base = dbPopulator.createBase(generateUniqueString())
    val project = base.project
    loginAsUser("admin")
    val response = mvc.perform(
      addToken(
        multipart("/api/project/${project.id}/screenshots")
          .file(
            MockMultipartFile(
              "screenshot", "originalShot.png", "image/png",
              screenshotFile.file.readBytes()
            )
          )
      )
    )
      .andExpect(status().isBadRequest).andReturn()

    assertThat(response).error().isStandardValidation.onField("key")
  }
}
