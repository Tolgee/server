package io.tolgee.repository.dataImport

import io.tolgee.AbstractSpringTest
import io.tolgee.assertions.Assertions.assertThat
import io.tolgee.development.testDataBuilder.data.ImportTestData
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.PageRequest
import org.testng.annotations.Test

@SpringBootTest
class ImportTranslationRepositoryTest : AbstractSpringTest() {

    @Autowired
    lateinit var importTranslationRepository: ImportTranslationRepository

    @Test
    fun `view returns correct data`() {
        val importTestData = ImportTestData()
        testDataService.saveTestData(importTestData.root)

        val result = importTranslationRepository
                .findImportTranslationsView(importTestData.importEnglish.id, PageRequest.of(0, 10))
        assertThat(result.content).hasSize(6)

        result.content[0].let {
            assertThat(it.id).isNotNull
            assertThat(it.keyName).isEqualTo("cool_key")
            assertThat(it.keyId).isNotNull
            assertThat(it.text).isEqualTo("test translation")
            assertThat(it.conflictText).isEqualTo("What a text")
            assertThat(it.conflictId).isNotNull
            assertThat(it.override).isEqualTo(false)
        }
    }

    @Test
    fun `view filters`() {
        val importTestData = ImportTestData()
        testDataService.saveTestData(importTestData.root)

        val result = importTranslationRepository
                .findImportTranslationsView(importTestData.importEnglish.id, PageRequest.of(0, 10), true)
        assertThat(result.content).hasSize(4)
    }
}
