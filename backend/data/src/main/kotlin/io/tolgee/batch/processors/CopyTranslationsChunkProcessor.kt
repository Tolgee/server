package io.tolgee.batch.processors

import io.tolgee.batch.BatchJobDto
import io.tolgee.batch.ChunkProcessor
import io.tolgee.batch.request.CopyTranslationRequest
import io.tolgee.model.batch.BatchJob
import io.tolgee.model.batch.CopyTranslationJobParams
import io.tolgee.service.translation.TranslationService
import kotlinx.coroutines.ensureActive
import org.springframework.stereotype.Component
import javax.persistence.EntityManager
import kotlin.coroutines.CoroutineContext

@Component
class CopyTranslationsChunkProcessor(
  private val translationService: TranslationService,
  private val entityManager: EntityManager
) : ChunkProcessor<CopyTranslationRequest> {
  override fun process(
    job: BatchJobDto,
    chunk: List<Long>,
    coroutineContext: CoroutineContext,
    onProgress: ((Int) -> Unit)
  ) {
    val subChunked = chunk.chunked(1000)
    var progress: Int = 0
    val params = getParams(job)
    subChunked.forEach { subChunk ->
      coroutineContext.ensureActive()
      translationService.copy(subChunk, params.sourceLanguageId, params.targetLanguageIds)
      entityManager.flush()
      progress += subChunk.size
      onProgress.invoke(progress)
    }
  }

  private fun getParams(job: BatchJobDto): CopyTranslationJobParams {
    return entityManager.createQuery(
      """from CopyTranslationJobParams ctjp where ctjp.batchJob.id = :batchJobId""",
      CopyTranslationJobParams::class.java
    )
      .setParameter("batchJobId", job.id).singleResult
      ?: throw IllegalStateException("No params found")
  }

  override fun getTarget(data: CopyTranslationRequest): List<Long> {
    return data.keyIds
  }

  override fun getParams(data: CopyTranslationRequest, job: BatchJob): CopyTranslationJobParams {
    return CopyTranslationJobParams().apply {
      batchJob = job
      sourceLanguageId = data.sourceLanguageId
      targetLanguageIds = data.targetLanguageIds
    }
  }
}
