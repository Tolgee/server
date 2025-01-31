package io.tolgee.api.v2.controllers.batch

import io.tolgee.ProjectAuthControllerTest
import io.tolgee.fixtures.andAssertThatJson
import io.tolgee.fixtures.andIsOk
import io.tolgee.fixtures.isValidId
import io.tolgee.model.batch.BatchJob
import io.tolgee.model.batch.BatchJobStatus
import io.tolgee.testing.annotations.ProjectJWTAuthTestMethod
import io.tolgee.testing.assert
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class BatchPreTranslateByMtTest : ProjectAuthControllerTest("/v2/projects/") {
  @Autowired
  lateinit var batchJobTestBase: BatchJobTestBase

  @BeforeEach
  fun setup() {
    batchJobTestBase.setup()
  }

  val testData
    get() = batchJobTestBase.testData

  @Test
  @ProjectJWTAuthTestMethod
  fun `it pre-translates by mt`() {
    val keyCount = 1000
    val keys = testData.addTranslationOperationData(keyCount)
    batchJobTestBase.saveAndPrepare(this)

    val keyIds = keys.map { it.id }.toList()

    performProjectAuthPost(
      "start-batch-job/pre-translate-by-tm",
      mapOf(
        "keyIds" to keyIds,
        "targetLanguageIds" to
          listOf(
            testData.projectBuilder.getLanguageByTag("cs")!!.self.id,
            testData.projectBuilder.getLanguageByTag("de")!!.self.id,
          ),
      ),
    )
      .andIsOk
      .andAssertThatJson {
        node("id").isValidId
      }

    batchJobTestBase.waitForAllTranslated(keyIds, keyCount, "cs")
    executeInNewTransaction {
      val jobs =
        entityManager.createQuery("""from BatchJob""", BatchJob::class.java)
          .resultList
      jobs.assert.hasSize(1)
      val job = jobs[0]
      job.status.assert.isEqualTo(BatchJobStatus.SUCCESS)
      job.activityRevision.assert.isNotNull
      job.activityRevision!!.modifiedEntities.assert.hasSize(2000)
    }
  }
}
