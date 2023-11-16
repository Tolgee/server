package io.tolgee.service.cdn

import io.tolgee.component.CdnStorageProvider
import io.tolgee.component.cdn.CdnUploader
import io.tolgee.dtos.request.CdnRequest
import io.tolgee.exceptions.NotFoundException
import io.tolgee.model.Project
import io.tolgee.model.cdn.Cdn
import io.tolgee.model.cdn.CdnStorage
import io.tolgee.repository.cdn.CdnRepository
import io.tolgee.service.automations.AutomationService
import io.tolgee.service.project.ProjectService
import io.tolgee.util.SlugGenerator
import org.springframework.context.annotation.Lazy
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import javax.persistence.EntityManager
import javax.transaction.Transactional
import kotlin.random.Random

@Service
class CdnService(
  private val cdnRepository: CdnRepository,
  private val slugGenerator: SlugGenerator,
  private val entityManager: EntityManager,
  private val cdnStorageProvider: CdnStorageProvider,
  private val projectService: ProjectService,
  private val automationService: AutomationService,
  @Lazy
  private val cdnUploader: CdnUploader
) {
  @Transactional
  fun create(projectId: Long, dto: CdnRequest): Cdn {
    val cdn = Cdn(entityManager.getReference(Project::class.java, projectId))
    cdn.name = dto.name
    cdn.cdnStorage = getStorage(projectId, dto.cdnStorageId)
    cdn.copyPropsFrom(dto)
    cdn.slug = generateSlug(projectId)
    cdnRepository.save(cdn)
    if (dto.autoPublish) {
      automationService.createForCdn(cdn)
      cdnUploader.upload(cdn.id)
    }
    return cdn
  }

  fun generateSlug(projectId: Long): String {
    val projectDto = projectService.getDto(projectId)

    return slugGenerator.generate(random32byteHexString(), 3, 50) {
      cdnRepository.isSlugUnique(projectDto.id, it)
    }
  }

  fun random32byteHexString(): String {
    return (1..32).joinToString("") { Random.nextInt(0, 16).toString(16) }
  }

  private fun getStorage(projectId: Long, cdnStorageId: Long?): CdnStorage? {
    cdnStorageId ?: return null
    return cdnStorageProvider.getStorage(projectId, cdnStorageId)
  }

  fun get(id: Long) = find(id) ?: throw NotFoundException()

  fun find(id: Long) = cdnRepository.findById(id).orElse(null)

  @Transactional
  fun update(projectId: Long, id: Long, dto: CdnRequest): Cdn {
    val exporter = get(projectId, id)
    exporter.cdnStorage = getStorage(projectId, dto.cdnStorageId)
    exporter.name = dto.name
    exporter.copyPropsFrom(dto)
    handleUpdateAutoPublish(dto, exporter)
    return cdnRepository.save(exporter)
  }

  private fun handleUpdateAutoPublish(dto: CdnRequest, exporter: Cdn) {
    if (dto.autoPublish && exporter.automationActions.isEmpty()) {
      automationService.createForCdn(exporter)
    }
    if (!dto.autoPublish && exporter.automationActions.isNotEmpty()) {
      automationService.removeForCdn(exporter)
    }
  }

  fun delete(projectId: Long, id: Long) {
    val cdn = get(projectId, id)
    cdn.automationActions.map { it.automation }.forEach {
      automationService.delete(it)
    }
    cdnRepository.deleteById(cdn.id)
  }

  fun getAllInProject(projectId: Long, pageable: Pageable): Page<Cdn> {
    return cdnRepository.findAllByProjectId(projectId, pageable)
  }

  fun get(projectId: Long, cdnId: Long): Cdn {
    return cdnRepository.getByProjectIdAndId(projectId, cdnId)
  }
}
