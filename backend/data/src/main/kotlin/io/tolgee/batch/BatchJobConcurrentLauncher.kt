package io.tolgee.batch

import io.sentry.Sentry
import io.tolgee.component.CurrentDateProvider
import io.tolgee.configuration.tolgee.BatchProperties
import io.tolgee.fixtures.waitFor
import io.tolgee.model.batch.BatchJobChunkExecutionStatus
import io.tolgee.util.Logging
import io.tolgee.util.logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap
import javax.annotation.PreDestroy
import kotlin.coroutines.CoroutineContext
import kotlin.math.ceil

@Component
class BatchJobConcurrentLauncher(
  private val batchProperties: BatchProperties,
  private val batchJobChunkExecutionQueue: BatchJobChunkExecutionQueue,
  private val currentDateProvider: CurrentDateProvider,
  private val batchJobProjectLockingManager: BatchJobProjectLockingManager,
  private val batchJobService: BatchJobService,
  private val progressManager: ProgressManager,
) : Logging {
  companion object {
    val runningInstances: ConcurrentHashMap.KeySetView<BatchJobConcurrentLauncher, Boolean> =
      ConcurrentHashMap.newKeySet()
  }

  /**
   * execution id -> Pair(BatchJobDto, Job)
   *
   * Job is the result of launch method executing the execution in separate coroutine
   */
  val runningJobs: ConcurrentHashMap<Long, Pair<BatchJobDto, Job>> = ConcurrentHashMap()

  var pause = false
    set(value) {
      field = value
      if (value) {
        waitFor(30000) {
          runningJobs.size == 0
        }
      }
    }

  var masterRunJob: Job? = null
  var run = true

  fun stop() {
    logger.trace("Stopping batch job launcher ${System.identityHashCode(this)}}")
    run = false
    runBlocking(Dispatchers.IO) {
      masterRunJob?.join()
    }
    logger.trace("Batch job launcher stopped ${System.identityHashCode(this)}")
    runningInstances.remove(this)
  }

  @PreDestroy
  fun preDestroy() {
    this.stop()
  }

  fun repeatForever(fn: () -> Boolean) {
    runningInstances.forEach { it.stop() }
    runningInstances.add(this)

    logger.trace("Started batch job action service ${System.identityHashCode(this)}")
    while (run) {
      try {
        val startTime = System.currentTimeMillis()
        val somethingHandled = fn()
        val sleepTime = getSleepTime(startTime, somethingHandled)
        if (sleepTime > 0) {
          Thread.sleep(sleepTime)
        }
      } catch (e: Throwable) {
        Sentry.captureException(e)
        logger.error("Error in batch job action service", e)
      }
    }
  }

  private fun getSleepTime(startTime: Long, somethingHandled: Boolean): Long {
    if (!batchJobChunkExecutionQueue.isEmpty() && jobsToLaunch > 0 && somethingHandled) {
      return 0
    }
    return BatchJobActionService.MIN_TIME_BETWEEN_OPERATIONS - (System.currentTimeMillis() - startTime)
  }

  fun run(processExecution: (executionItem: ExecutionQueueItem, coroutineContext: CoroutineContext) -> Unit) {
    @Suppress("OPT_IN_USAGE")
    masterRunJob = GlobalScope.launch(Dispatchers.IO) {
      repeatForever {
        if (pause) {
          return@repeatForever false
        }

        val jobsToLaunch = jobsToLaunch
        if (jobsToLaunch <= 0) {
          return@repeatForever false
        }

        logger.trace("Jobs to launch: $jobsToLaunch")
        val items = (1..jobsToLaunch)
          .mapNotNull { batchJobChunkExecutionQueue.poll() }

        logItemsPulled(items)

        // when something handled, return true
        items.map { executionItem ->
          handleItem(executionItem, processExecution)
        }.any()
      }
    }
  }

  private fun logItemsPulled(items: List<ExecutionQueueItem>) {
    if (items.isNotEmpty()) {
      logger.trace(
        "Pulled ${items.size} items from queue: " +
          items.joinToString(", ") { it.chunkExecutionId.toString() }
      )
      logger.trace(
        "${batchJobChunkExecutionQueue.size} is left in the queue " +
          "(${System.identityHashCode(batchJobChunkExecutionQueue)}): " +
          batchJobChunkExecutionQueue.joinToString(", ") { it.chunkExecutionId.toString() }
      )
    }
  }

  /**
   * Returns true if item was handled
   */
  private fun CoroutineScope.handleItem(
    executionItem: ExecutionQueueItem,
    processExecution: (executionItem: ExecutionQueueItem, coroutineContext: CoroutineContext) -> Unit
  ): Boolean {
    logger.trace("Trying to run execution ${executionItem.chunkExecutionId}")
    if (!executionItem.isTimeToExecute()) {
      logger.trace(
        """Execution ${executionItem.chunkExecutionId} not ready to execute, adding back to queue:
                    | Difference ${executionItem.executeAfter!! - currentDateProvider.date.time}""".trimMargin()
      )
      addBackToQueue(executionItem)
      return false
    }
    if (!canRunJobWithCharacter(executionItem.jobCharacter)) {
      logger.trace(
        """Execution ${executionItem.chunkExecutionId} cannot run concurrent job 
          |(there are already max coroutines working on this specific job)""".trimMargin()
      )
      addBackToQueue(executionItem)
      return false
    }

    if (!executionItem.trySetRunningState()) {
      logger.trace(
        """Execution ${executionItem.chunkExecutionId} cannot run concurrent job 
          |(there are already max concurrent executions running of this specific job)""".trimMargin()
      )
      addBackToQueue(executionItem)
      return false
    }

    /**
     * Only single job can run in project at the same time
     */
    if (!batchJobProjectLockingManager.canRunBatchJobOfExecution(executionItem.jobId)) {
      logger.debug("⚠️ Cannot run execution ${executionItem.chunkExecutionId}. Other job from the project is currently running, skipping")

      // we haven't publish consuming, so we can add it only to the local queue
      batchJobChunkExecutionQueue.addItemsToLocalQueue(listOf(executionItem))
      return
    }

    val job = launch {
      processExecution(executionItem, this.coroutineContext)
    }

    val batchJobDto = batchJobService.getJobDto(executionItem.jobId)
    runningJobs[executionItem.chunkExecutionId] = batchJobDto to job

    job.invokeOnCompletion {
      onJobCompleted(executionItem)
    }
    logger.debug("Execution ${executionItem.chunkExecutionId} launched. Running jobs: ${runningJobs.size}")
    return true
  }

  private fun addBackToQueue(executionItem: ExecutionQueueItem) {
    logger.trace("Adding execution $executionItem back to queue")
    batchJobChunkExecutionQueue.addItemsToLocalQueue(listOf(executionItem))
  }

  private fun onJobCompleted(executionItem: ExecutionQueueItem) {
    runningJobs.remove(executionItem.chunkExecutionId)
    logger.debug("Chunk ${executionItem.chunkExecutionId}: Completed")
    logger.debug("Running jobs: ${runningJobs.size}")
  }

  private val jobsToLaunch get() = batchProperties.concurrency - runningJobs.size

  fun ExecutionQueueItem.isTimeToExecute(): Boolean {
    val executeAfter = this.executeAfter ?: return true
    return executeAfter <= currentDateProvider.date.time
  }

  private fun canRunJobWithCharacter(character: JobCharacter): Boolean {
    val queueCharacterCounts = batchJobChunkExecutionQueue.getJobCharacterCounts()
    val otherCharactersInQueueCount = queueCharacterCounts.filter { it.key != character }.values.sum()
    if (otherCharactersInQueueCount == 0) {
      return true
    }
    val runningJobCharacterCounts = runningJobs.values.filter { it.first.jobCharacter == character }.size
    val allowedCharacterCounts = ceil(character.maxConcurrencyRatio * batchProperties.concurrency)
    return runningJobCharacterCounts < allowedCharacterCounts
  }

  private fun ExecutionQueueItem.trySetRunningState(): Boolean {
    return progressManager.trySetExecutionRunning(this.chunkExecutionId, this.jobId) {
      val count = it.values.count { executionState -> executionState.status == BatchJobChunkExecutionStatus.RUNNING }
      if (count == 0) {
        return@trySetExecutionRunning true
      }
      batchJobService.getJobDto(this.jobId).maxPerJobConcurrency > count
    }
  }
}
