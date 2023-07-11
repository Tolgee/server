package io.tolgee.batch.events

import io.tolgee.batch.BatchJobDto
import io.tolgee.batch.OnBatchJobCompleted

data class OnBatchJobCancelled(
  override val job: BatchJobDto,
) : OnBatchJobCompleted
