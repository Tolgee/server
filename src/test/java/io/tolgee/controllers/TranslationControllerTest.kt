package io.tolgee.controllers

import io.tolgee.annotations.ProjectApiKeyAuthTestMethod
import io.tolgee.assertions.Assertions.assertThat
import io.tolgee.constants.ApiScope
import io.tolgee.dtos.PathDTO
import io.tolgee.dtos.request.SetTranslationsDTO
import io.tolgee.dtos.response.KeyWithTranslationsResponseDto
import io.tolgee.dtos.response.ViewDataResponse
import io.tolgee.dtos.response.translations_view.ResponseParams
import io.tolgee.fixtures.andIsForbidden
import io.tolgee.fixtures.generateUniqueString
import io.tolgee.fixtures.mapResponseTo
import io.tolgee.helpers.JsonHelper
import io.tolgee.model.Project
import org.assertj.core.api.Assertions
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.testng.annotations.Test

@SpringBootTest
@AutoConfigureMockMvc
class TranslationControllerTest()
    : ProjectAuthControllerTest() {

    @Test
    fun getViewDataSearch() {
        val app = dbPopulator.populate(generateUniqueString())
        val searchString = "This"
        val response = performValidViewRequest(app, "?search=$searchString")
        Assertions.assertThat(response.data.size).isPositive
        assertThat(JsonHelper.asJsonString(response)).contains(searchString)
    }

    @Test
    fun getViewDataQueryLanguages() {
        val project = dbPopulator.populate(generateUniqueString())
        val response = performValidViewRequest(project, "?languages=en")
        Assertions.assertThat(response.data.size).isGreaterThan(8)
        for ((_, _, translations) in response.data) {
            Assertions.assertThat(translations).doesNotContainKeys("de")
        }
        performGetDataForView(project.id, "?languages=langNotExists").andExpect(status().isNotFound)

        val result = performValidViewRequest(project, "?languages=en,en")
        assertThat(result.data).isNotEmpty
    }

    private fun performValidViewRequest(project: Project, queryString: String):
            ViewDataResponse<LinkedHashSet<KeyWithTranslationsResponseDto>, ResponseParams> {
        val mvcResult = performGetDataForView(project.id, queryString).andExpect(status().isOk).andReturn()
        return mvcResult.mapResponseTo()
    }

    @Test
    fun getViewDataQueryPagination() {
        val project = dbPopulator.populate(generateUniqueString())
        val limit = 5
        val response = performValidViewRequest(project, String.format("?limit=%d", limit))
        Assertions.assertThat(response.data.size).isEqualTo(limit)
        Assertions.assertThat(response.paginationMeta.allCount).isEqualTo(12)
        Assertions.assertThat(response.paginationMeta.offset).isZero
        val offset = 3
        val responseOffset = performValidViewRequest(project, String.format("?limit=%d&offset=%d", limit, offset))
        Assertions.assertThat(responseOffset.data.size).isEqualTo(limit)
        Assertions.assertThat(responseOffset.paginationMeta.offset).isEqualTo(offset)
        response.data.stream().limit(offset.toLong())
                .forEach { i: KeyWithTranslationsResponseDto ->
                    Assertions.assertThat(responseOffset.data).doesNotContain(i)
                }
        response.data.stream().skip(offset.toLong())
                .forEach { i: KeyWithTranslationsResponseDto ->
                    Assertions.assertThat(responseOffset.data).contains(i)
                }
    }

    @Test
    fun getViewDataMetadata() {
        val project = dbPopulator.populate(generateUniqueString())
        val limit = 5
        val response = performValidViewRequest(project, String.format("?limit=%d", limit))
        Assertions.assertThat(response.params.languages).contains("en", "de")
    }

    @Test
    fun getTranslations() {
        val project = dbPopulator.populate(generateUniqueString())
        val mvcResult = performAuthGet("/api/project/${project.id}/translations/en,de")
                .andExpect(status().isOk).andReturn()
        val result: Map<String, Any> = mvcResult.mapResponseTo()
        assertThat(result).containsKeys("en", "de")
    }

    @Test
    fun setTranslations() {
        val project = dbPopulator.createBase(generateUniqueString())
        val translationsMap = mapOf(Pair("en", "Hello"), Pair("de", "Hallo"));

        performAuthPost("/api/project/${project.id}/translations", SetTranslationsDTO("hello",
                translationsMap
        )).andExpect(status().isOk)

        val fromService = translationService.getKeyTranslationsResult(
                project.id,
                PathDTO.fromFullPath("hello"),
                setOf("en", "de")
        )

        assertThat(fromService).isEqualTo(translationsMap)
    }

    @Test
    @ProjectApiKeyAuthTestMethod
    fun setTranslationsWithApiKey() {
        val translationsMap = mapOf(Pair("en", "Hello"), Pair("de", "Hallo"));

        performProjectAuthPost("translations", SetTranslationsDTO("hello",
                translationsMap
        )).andExpect(status().isOk)

        val fromService = translationService.getKeyTranslationsResult(
                project.id,
                PathDTO.fromFullPath("hello"),
                setOf("en", "de")
        )

        assertThat(fromService).isEqualTo(translationsMap)
    }

    @Test
    @ProjectApiKeyAuthTestMethod(scopes = [ApiScope.TRANSLATIONS_EDIT])
    fun setTranslationsWithApiKeyForbidden() {
        val translationsMap = mapOf(Pair("en", "Hello"), Pair("de", "Hallo"));

        performProjectAuthPost("translations", SetTranslationsDTO("hello",
                translationsMap
        )).andIsForbidden
    }

    @Test
    fun updateTranslations() {
        val project = dbPopulator.createBase(generateUniqueString())
        val translationsMap = mapOf(Pair("en", "Hello"), Pair("de", "Hallo"));
        keyService.create(project, SetTranslationsDTO(key = "hello", translations = translationsMap));


        val updatedTranslationsMap = mapOf(Pair("en", "Hello you!"), Pair("de", "Hallo dich!"))
        performAuthPut("/api/project/${project.id}/translations", SetTranslationsDTO("hello",
                updatedTranslationsMap
        )).andExpect(status().isOk)

        val fromService = translationService.getKeyTranslationsResult(
                project.id,
                PathDTO.fromFullPath("hello"),
                setOf("en", "de")
        )

        assertThat(fromService).isEqualTo(updatedTranslationsMap)
    }

    private fun performGetDataForView(projectId: Long, queryString: String): ResultActions {
        return performAuthGet("/api/project/$projectId/translations/view$queryString")
    }
}
