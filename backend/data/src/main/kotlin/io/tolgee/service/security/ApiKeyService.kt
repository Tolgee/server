package io.tolgee.service.security

import com.google.common.io.BaseEncoding
import io.sentry.Sentry
import io.tolgee.component.CurrentDateProvider
import io.tolgee.component.KeyGenerator
import io.tolgee.constants.Message
import io.tolgee.dtos.request.apiKey.V2EditApiKeyDto
import io.tolgee.exceptions.NotFoundException
import io.tolgee.model.ApiKey
import io.tolgee.model.Project
import io.tolgee.model.UserAccount
import io.tolgee.model.enums.ApiScope
import io.tolgee.repository.ApiKeyRepository
import io.tolgee.security.PAT_PREFIX
import io.tolgee.security.PROJECT_API_KEY_PREFIX
import io.tolgee.util.runSentryCatching
import org.springframework.context.annotation.Lazy
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class ApiKeyService(
  private val apiKeyRepository: ApiKeyRepository,
  private val keyGenerator: KeyGenerator,
  private val currentDateProvider: CurrentDateProvider,
  @Lazy
  private val permissionService: PermissionService
) {
  fun create(
    userAccount: UserAccount,
    scopes: Set<ApiScope>,
    project: Project,
    expiresAt: Long? = null,
    description: String? = null
  ): ApiKey {
    val apiKey = ApiKey(
      key = generateKey(),
      project = project,
      userAccount = userAccount,
      scopesEnum = scopes
    ).apply {
      this.description = description ?: ""
      this.expiresAt = expiresAt?.let { Date(expiresAt) }
    }
    return save(apiKey)
  }

  private fun generateKey() = keyGenerator.generate(130)

  fun getAllByUser(userAccountId: Long): Set<ApiKey> {
    return apiKeyRepository.getAllByUserAccountIdOrderById(userAccountId)
  }

  fun getAllByUser(userAccountId: Long, filterProjectId: Long?, pageable: Pageable): Page<ApiKey> {
    return apiKeyRepository.getAllByUserAccount(userAccountId, filterProjectId, pageable)
  }

  fun getAllByProject(projectId: Long): Set<ApiKey> {
    return apiKeyRepository.getAllByProjectId(projectId)
  }

  fun getAllByProject(projectId: Long, pageable: Pageable): Page<ApiKey> {
    return apiKeyRepository.getAllByProjectId(projectId, pageable)
  }

  fun findOptional(apiKey: String): Optional<ApiKey> {
    return apiKeyRepository.findByKeyHash(apiKey)
  }

  fun findOptional(id: Long): Optional<ApiKey> {
    return apiKeyRepository.findById(id)
  }

  fun find(apiKey: String): ApiKey? {
    return apiKeyRepository.findByKeyHash(apiKey).orElse(null)
  }

  fun find(id: Long): ApiKey? {
    return apiKeyRepository.findById(id).orElse(null)
  }

  fun get(id: Long): ApiKey {
    return find(id) ?: throw NotFoundException(Message.API_KEY_NOT_FOUND)
  }

  fun deleteApiKey(apiKey: ApiKey) {
    apiKeyRepository.delete(apiKey)
  }

  fun getAvailableScopes(userAccountId: Long, project: Project): Set<ApiScope> {
    return permissionService.getProjectPermissionType(project.id, userAccountId)?.availableScopes?.toSet()
      ?: throw NotFoundException()
  }

  fun editApiKey(apiKey: ApiKey, dto: V2EditApiKeyDto): ApiKey {
    apiKey.scopesEnum = dto.scopes.toMutableSet()
    dto.description?.let {
      apiKey.description = it
    }
    return save(apiKey)
  }

  fun deleteAllByProject(projectId: Long) {
    apiKeyRepository.deleteAllByProjectId(projectId)
  }

  fun saveAll(entities: Iterable<ApiKey>) {
    entities.forEach {
      save(it)
    }
  }

  fun hashKey(key: String) = keyGenerator.hash(key)

  fun save(entity: ApiKey): ApiKey {
    entity.key?.let { key ->
      entity.keyHash = hashKey(key)
      entity.encodedKey = encodeKey(key, entity.project.id)
      if (entity.description.isBlank()) {
        entity.description = "${key.take(5)}......${key.takeLast(5)}"
      }
    }

    return this.apiKeyRepository.save(entity)
  }

  fun encodeKey(key: String, projectId: Long): String {
    val stringToEncode = "${projectId}_$key"
    return BaseEncoding.base32().omitPadding().lowerCase().encode(stringToEncode.toByteArray())
  }

  fun decodeKey(raw: String): DecodedApiKey? {
    try {
      val decoded = BaseEncoding.base32().omitPadding().lowerCase().decode(raw).decodeToString()

      val (projectId, key) = decoded.split("_".toRegex(), 2)
      return DecodedApiKey(projectId.toLong(), key)
    } catch (e: IllegalArgumentException) {
      return null
    } catch (e: IndexOutOfBoundsException) {
      return null
    } catch (e: Exception) {
      Sentry.captureException(e)
      return null
    }
  }

  @Async
  @Transactional
  fun updateLastUsedAsync(apiKey: ApiKey) {
    runSentryCatching {
      updateLastUsed(apiKey)
    }
  }

  fun updateLastUsed(apiKey: ApiKey) {
    apiKey.lastUsedAt = currentDateProvider.date
    save(apiKey)
  }

  fun regenerate(id: Long, expiresAt: Long?): ApiKey {
    val apiKey = get(id)
    apiKey.key = generateKey()
    apiKey.expiresAt = expiresAt?.let { Date(it) }
    return save(apiKey)
  }

  /**
   * Parses API key from header or query param
   */
  fun parseApiKey(rawWithPossiblePrefix: String?): String? {
    if (rawWithPossiblePrefix.isNullOrBlank()) {
      return null
    }

    if (rawWithPossiblePrefix.startsWith(PROJECT_API_KEY_PREFIX)) {
      val raw = rawWithPossiblePrefix.substring(PROJECT_API_KEY_PREFIX.length)
      return this.decodeKey(raw)?.apiKey
    }

    if (rawWithPossiblePrefix.startsWith(PAT_PREFIX)) {
      return null
    }

    // probably legacy project api key without any prefix
    return rawWithPossiblePrefix
  }

  class DecodedApiKey(
    val projectId: Long,
    val apiKey: String
  )
}
