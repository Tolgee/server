package io.tolgee.batch

import io.tolgee.activity.ActivityHolder
import io.tolgee.fixtures.waitFor
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import javax.persistence.EntityManager

@Component
class BatchJobActivityFinalizer(
  private val entityManager: EntityManager,
  private val atomicProgressState: AtomicProgressState,
  private val activityHolder: ActivityHolder
) {
  @EventListener(OnBatchOperationSucceeded::class)
  fun finalizeActivityWhenJobSucceeded(event: OnBatchOperationSucceeded) {
    finalizeActivityWhenJobCompleted(event.job)
  }

  @EventListener(OnBatchOperationFailed::class)
  fun finalizeActivityWhenJobFailed(event: OnBatchOperationFailed) {
    finalizeActivityWhenJobCompleted(event.job)
  }

  fun finalizeActivityWhenJobCompleted(job: BatchJobDto) {
    val activityRevision =
      activityHolder.activityRevision ?: throw IllegalStateException("Activity revision is not set")

    activityRevision.afterFlush = afterFlush@{
      waitForOtherChunksToComplete(job)
      val revisionIds = getRevisionIds(job.id)

      val activityRevisionIdToMergeInto = revisionIds.firstOrNull() ?: return@afterFlush
      revisionIds.remove(activityRevisionIdToMergeInto)

      mergeDescribingEntities(activityRevisionIdToMergeInto, revisionIds)
      mergeModifiedEntities(activityRevisionIdToMergeInto, revisionIds)
      deleteUnusedRevisions(revisionIds)
      setJobIdToRevision(activityRevisionIdToMergeInto, job.id)
    }
  }

  private fun waitForOtherChunksToComplete(job: BatchJobDto) {
    waitFor(20000) {
      atomicProgressState.getCompletedChunksCommittedAtomicLong(job.id).get() == job.totalChunks.toLong() - 1
    }
  }

  private fun setJobIdToRevision(activityRevisionIdToMergeInto: Long, jobId: Long) {
    entityManager.createNativeQuery(
      """
        update activity_revision set batch_job_chunk_execution_id = null, batch_job_id = :jobId
        where id = :activityRevisionIdToMergeInto
        """
    )
      .setParameter("activityRevisionIdToMergeInto", activityRevisionIdToMergeInto)
      .setParameter("jobId", jobId)
      .executeUpdate()
  }

  private fun deleteUnusedRevisions(revisionIds: MutableList<Long>) {
    entityManager.createNativeQuery(
      """
          delete from activity_revision where id in (:revisionIds)
        """
    )
      .setParameter("revisionIds", revisionIds)
      .executeUpdate()
  }

  private fun mergeModifiedEntities(
    activityRevisionIdToMergeInto: Long,
    revisionIds: MutableList<Long>
  ) {
    entityManager.createNativeQuery(
      """
        update activity_modified_entity set activity_revision_id = :activityRevisionIdToMergeInto
        where activity_revision_id in (:revisionIds)
        """
    )
      .setParameter("activityRevisionIdToMergeInto", activityRevisionIdToMergeInto)
      .setParameter("revisionIds", revisionIds)
      .executeUpdate()
  }

  private fun mergeDescribingEntities(
    activityRevisionIdToMergeInto: Long,
    revisionIds: MutableList<Long>
  ) {
    removeDuplicitDescribingEntities(activityRevisionIdToMergeInto, revisionIds)

    entityManager.createNativeQuery(
      """
        update activity_describing_entity set activity_revision_id = :activityRevisionIdToMergeInto
        where activity_revision_id in (:revisionIds)
        """
    )
      .setParameter("activityRevisionIdToMergeInto", activityRevisionIdToMergeInto)
      .setParameter("revisionIds", revisionIds)
      .executeUpdate()
  }

  private fun removeDuplicitDescribingEntities(
    activityRevisionIdToMergeInto: Long,
    revisionIds: MutableList<Long>
  ) {
    entityManager.createNativeQuery(
      """
        delete from activity_describing_entity
        where (entity_class, entity_id) in
              (select entity_class, entity_id
               from activity_describing_entity
               where activity_revision_id in (:revisionIds)
                  or activity_revision_id = :activityRevisionIdToMergeInto
               group by entity_class, entity_id
               having count(*) > 1)
        and
            activity_revision_id not in (select min(activity_revision_id)
         from activity_describing_entity
         where activity_revision_id in (:revisionIds)
            or activity_revision_id = :activityRevisionIdToMergeInto
         group by entity_class, entity_id
         having count(*) > 1)
      """.trimIndent()
    )
      .setParameter("activityRevisionIdToMergeInto", activityRevisionIdToMergeInto)
      .setParameter("revisionIds", revisionIds)
      .executeUpdate()
  }

  private fun getRevisionIds(jobId: Long): MutableList<Long> = entityManager.createQuery(
    """
        select ar.id
        from ActivityRevision ar
        join ar.batchJobChunkExecution b
        where b.batchJob.id = :jobId
      """,
    Long::class.javaObjectType
  )
    .setParameter("jobId", jobId)
    .resultList
}
