package io.tolgee.service.project

import io.tolgee.component.LockingProvider
import io.tolgee.exceptions.NotFoundException
import io.tolgee.model.LanguageStats
import io.tolgee.model.views.projectStats.ProjectLanguageStatsResultView
import io.tolgee.repository.LanguageStatsRepository
import io.tolgee.service.LanguageService
import io.tolgee.service.query_builders.LanguageStatsProvider
import io.tolgee.util.Logging
import io.tolgee.util.executeInNewTransaction
import io.tolgee.util.logger
import org.springframework.stereotype.Service
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.annotation.Transactional
import java.util.concurrent.TimeUnit
import javax.persistence.EntityManager

@Transactional
@Service
class LanguageStatsService(
  private val languageService: LanguageService,
  private val projectStatsService: ProjectStatsService,
  private val languageStatsRepository: LanguageStatsRepository,
  private val entityManager: EntityManager,
  private val projectService: ProjectService,
  private val lockingProvider: LockingProvider,
  private val platformTransactionManager: PlatformTransactionManager
) : Logging {
  fun refreshLanguageStats(projectId: Long) {
    withLocking(projectId) {
      executeInNewTransaction(platformTransactionManager) {
        val languages = languageService.findAll(projectId)
        val allRawLanguageStats = getLanguageStatsRaw(projectId)
        try {

          val baseLanguage = projectService.getOrCreateBaseLanguage(projectId)
          val rawBaseLanguageStats = allRawLanguageStats.find { it.languageId == baseLanguage?.id }
            ?: return@executeInNewTransaction
          val projectStats = projectStatsService.getProjectStats(projectId)
          val languageStats = languageStatsRepository.getAllByProjectId(projectId)
            .associateBy { it.language.id }
            .toMutableMap()

          allRawLanguageStats
            .sortedBy { it.languageName }
            .sortedBy { it.languageId != rawBaseLanguageStats.languageId }
            .forEach { rawLanguageStats ->
              val baseWords = rawBaseLanguageStats.translatedWords + rawBaseLanguageStats.reviewedWords
              val translatedOrReviewedKeys = rawLanguageStats.translatedKeys + rawLanguageStats.reviewedKeys
              val translatedOrReviewedWords = rawLanguageStats.translatedWords + rawLanguageStats.reviewedWords
              val untranslatedWords = baseWords - translatedOrReviewedWords
              val language = languages.find { it.id == rawLanguageStats.languageId } ?: return@executeInNewTransaction
              val stats = languageStats.computeIfAbsent(language.id) {
                LanguageStats(language)
              }
              stats.apply {
                translatedKeys = rawLanguageStats.translatedKeys
                translatedWords = rawLanguageStats.translatedWords
                translatedPercentage = rawLanguageStats.translatedWords.toDouble() / baseWords * 100
                reviewedKeys = rawLanguageStats.reviewedKeys
                reviewedWords = rawLanguageStats.reviewedWords
                reviewedPercentage = rawLanguageStats.reviewedWords.toDouble() / baseWords * 100
                untranslatedKeys = projectStats.keyCount - translatedOrReviewedKeys
                this.untranslatedWords = baseWords - translatedOrReviewedWords
                untranslatedPercentage = untranslatedWords.toDouble() / baseWords * 100
              }
            }

          languageStats.values.forEach {
            it.language.stats = it
            languageStatsRepository.save(it)
          }
        } catch (e: NotFoundException) {
          logger.warn("Cannot save Language Stats due to NotFoundException. Project deleted too fast?")
        }
      }
    }
  }

  private fun withLocking(projectId: Long, fn: () -> Unit) {
    val lock = lockingProvider.getLock("refresh-language-stats$projectId")
    if (!lock.tryLock(60, TimeUnit.SECONDS)) {
      // if not satisfied in 60 seconds, it probably still locked because of server shut down or etc
      lock.unlock()
    }
    try {
      fn()
    } finally {
      lock.unlock()
    }
  }

  fun getLanguageStats(projectIds: List<Long>): Map<Long, List<LanguageStats>> {
    val data = languageStatsRepository.getAllByProjectIds(projectIds).groupByProjects()

    val emptyProjectsIds = projectIds.filter { data[it].isNullOrEmpty() }
    if (emptyProjectsIds.isEmpty()) {
      return data
    }

    emptyProjectsIds.forEach {
      refreshLanguageStats(it)
    }

    return languageStatsRepository.getAllByProjectIds(projectIds).groupByProjects()
  }

  private fun List<LanguageStats>.groupByProjects(): Map<Long, List<LanguageStats>> {
    return this.groupBy { it.language.project.id }
  }

  fun getLanguageStats(projectId: Long): List<LanguageStats> {
    val languageStats = languageStatsRepository.getAllByProjectId(projectId)
    // when no stats are populated yet, we can try to do it now
    if (languageStats.isNotEmpty()) {
      return languageStats
    }
    refreshLanguageStats(projectId)
    return languageStatsRepository.getAllByProjectId(projectId)
  }

  fun deleteAllByLanguage(languageId: Long) {
    languageStatsRepository.deleteAllByLanguage(languageId)
  }

  private fun getLanguageStatsRaw(projectId: Long): List<ProjectLanguageStatsResultView> {
    return LanguageStatsProvider(entityManager, listOf(projectId)).getResultForSingleProject()
  }
}
