package io.tolgee.activity

import io.tolgee.activity.data.ActivityType
import io.tolgee.activity.projectActivityView.ProjectActivityViewDataProvider
import io.tolgee.dtos.query_results.TranslationHistoryView
import io.tolgee.events.OnProjectActivityStoredEvent
import io.tolgee.model.views.activity.ProjectActivityView
import io.tolgee.repository.activity.ActivityModifiedEntityRepository
import io.tolgee.util.executeInNewTransaction
import org.springframework.context.ApplicationContext
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.annotation.Transactional
import javax.persistence.EntityManager

@Service
class ActivityService(
  private val entityManager: EntityManager,
  private val transactionManager: PlatformTransactionManager,
  private val applicationContext: ApplicationContext,
  private val activityModifiedEntityRepository: ActivityModifiedEntityRepository
) {
  @Transactional
  fun storeActivityData(activityHolder: ActivityHolder) {
    val activityRevision = activityHolder.activityRevision ?: return
    val modifiedEntities = activityHolder.modifiedEntities
    activityRevision.modifiedEntities = activityHolder.modifiedEntities.values.flatMap { it.values }.toMutableList()

    executeInNewTransaction(transactionManager) {
      entityManager.persist(activityRevision)
      activityRevision.describingRelations.forEach {
        entityManager.persist(it)
      }
      activityRevision.modifiedEntities.forEach { activityModifiedEntity ->
        entityManager.persist(activityModifiedEntity)
      }
    }
    applicationContext.publishEvent(OnProjectActivityStoredEvent(this, activityRevision))
  }

  @Transactional
  fun getProjectActivity(projectId: Long, pageable: Pageable): Page<ProjectActivityView> {
    return ProjectActivityViewDataProvider(
      applicationContext = applicationContext,
      projectId = projectId,
      pageable = pageable
    ).getProjectActivity()
  }

  @Transactional
  fun getTranslationHistory(translationId: Long, pageable: Pageable): Page<TranslationHistoryView> {
    return activityModifiedEntityRepository.getTranslationHistory(
      translationId = translationId,
      pageable = pageable,
      ignoredActivityTypes = listOf(ActivityType.TRANSLATION_COMMENT_ADD)
    )
  }
}
