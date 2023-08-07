package io.tolgee.batch

import io.sentry.Sentry
import io.tolgee.activity.ActivityHolder
import io.tolgee.component.CurrentDateProvider
import io.tolgee.exceptions.ExceptionWithMessage
import io.tolgee.model.batch.BatchJobChunkExecution
import io.tolgee.model.batch.BatchJobChunkExecutionStatus
import io.tolgee.util.Logging
import io.tolgee.util.logger
import org.hibernate.LockOptions
import org.springframework.context.ApplicationContext
import java.util.*
import javax.persistence.EntityManager
import kotlin.coroutines.CoroutineContext
import kotlin.math.pow
import kotlin.system.measureTimeMillis

open class ChunkProcessingUtil(
  val execution: BatchJobChunkExecution,
  private val applicationContext: ApplicationContext,
  private val coroutineContext: CoroutineContext,
) : Logging {
  open fun processChunk() {
    val time = measureTimeMillis {
      try {
        val processor = batchJobService.getProcessor<Any, Any>(job.type)
        processor.process(job, toProcess, coroutineContext) {
          if (it != toProcess.size) {
            progressManager.publishSingleChunkProgress(job.id, it)
          }
        }
        successfulTargets = toProcess
        execution.status = BatchJobChunkExecutionStatus.SUCCESS
        handleActivity()
      } catch (e: Throwable) {
        handleException(e)
      } finally {
        successfulTargets?.let {
          execution.successTargets = it
        }
      }
    }
    logger.debug("Chunk ${execution.id} executed in ${time}ms")
  }

  private fun handleActivity() {
    val activityRevision = activityHolder.activityRevision
    activityRevision.batchJobChunkExecution = execution
    val batchJobDto = batchJobService.getJobDto(job.id)
    activityRevision.projectId = batchJobDto.projectId
    activityHolder.activity = batchJobDto.type.activityType
  }

  private fun handleException(exception: Throwable) {
    if (exception is kotlinx.coroutines.CancellationException) {
      execution.status = BatchJobChunkExecutionStatus.CANCELLED
      return
    }

    execution.exception = exception.stackTraceToString()
    execution.status = BatchJobChunkExecutionStatus.FAILED
    execution.errorMessage = (exception as? ExceptionWithMessage)?.tolgeeMessage

    Sentry.captureException(exception)
    logger.error(exception.message, exception)

    if (exception is ChunkFailedException) {
      successfulTargets = exception.successfulTargets
      successfulTargets?.let { execution.successTargets = it }
    }

    if (exception is FailedDontRequeueException) {
      return
    }

    retryFailedExecution(exception)
  }

  private fun retryFailedExecution(exception: Throwable) {
    var maxRetries = job.type.maxRetries
    var waitTime = job.type.defaultRetryWaitTimeInMs

    if (exception is RequeueWithDelayException) {
      maxRetries = exception.maxRetries
      waitTime = getWaitTime(exception)
    }

    if (retries >= maxRetries) {
      logger.debug("Max retries reached for job execution $execution")
      Sentry.captureException(exception)
      return
    }

    logger.debug("Retrying job execution $execution in ${waitTime}ms")
    retryExecution.executeAfter = Date(waitTime + currentDateProvider.date.time)
    execution.retry = true
  }

  private fun getWaitTime(exception: RequeueWithDelayException) =
    exception.delayInMs * (exception.increaseFactor.toDouble().pow(retries.toDouble())).toInt()

  private val job by lazy { batchJobService.getJobDto(execution.batchJob.id) }

  private val activityHolder by lazy {
    applicationContext.getBean(ActivityHolder::class.java)
  }

  private val entityManager by lazy {
    applicationContext.getBean(EntityManager::class.java)
  }

  private val currentDateProvider by lazy {
    applicationContext.getBean(CurrentDateProvider::class.java)
  }

  private val batchJobService by lazy {
    applicationContext.getBean(BatchJobService::class.java)
  }

  private val progressManager by lazy {
    applicationContext.getBean(ProgressManager::class.java)
  }

  private var successfulTargets: List<Long>? = null

  private val toProcess by lazy {
    val chunked = job.chunkedTarget
    val chunk = chunked[execution.chunkNumber]
    val previousSuccessfulTargets = previousExecutions.flatMap { it.successTargets }.toSet()
    val toProcess = chunk.toMutableSet()
    toProcess.removeAll(previousSuccessfulTargets)
    toProcess.toList()
  }

  val retryExecution: BatchJobChunkExecution by lazy {
    BatchJobChunkExecution().apply {
      batchJob = entityManager.getReference(execution.batchJob::class.java, job.id)
      chunkNumber = execution.chunkNumber
      status = BatchJobChunkExecutionStatus.PENDING
    }
  }

  private val retries: Int by lazy {
    previousExecutions.size
  }

  @Suppress("UNCHECKED_CAST")
  private val previousExecutions: List<BatchJobChunkExecution> by lazy {
    entityManager.createQuery(
      """
      from BatchJobChunkExecution 
      where chunkNumber = :chunkNumber 
          and batchJob.id = :batchJobId
          and status = :status
      """.trimIndent()
    )
      .setParameter("chunkNumber", execution.chunkNumber)
      .setParameter("batchJobId", job.id)
      .setParameter("status", BatchJobChunkExecutionStatus.FAILED)
      .setHint(
        "javax.persistence.lock.timeout",
        LockOptions.NO_WAIT
      )
      .resultList as List<BatchJobChunkExecution>
  }
}
