package io.tolgee.service

import io.tolgee.configuration.tolgee.TolgeeProperties
import io.tolgee.constants.Caches
import io.tolgee.constants.Message
import io.tolgee.dtos.cacheable.UserAccountDto
import io.tolgee.dtos.request.UserUpdateRequestDto
import io.tolgee.dtos.request.auth.SignUpDto
import io.tolgee.dtos.request.validators.exceptions.ValidationException
import io.tolgee.model.UserAccount
import io.tolgee.model.views.UserAccountInProjectView
import io.tolgee.model.views.UserAccountWithOrganizationRoleView
import io.tolgee.repository.UserAccountRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class UserAccountService(
  private val userAccountRepository: UserAccountRepository,
  private val tolgeeProperties: TolgeeProperties,
) {
  @Autowired
  lateinit var emailVerificationService: EmailVerificationService

  fun getByUserName(username: String?): Optional<UserAccount> {
    return userAccountRepository.findByUsername(username)
  }

  operator fun get(id: Long): Optional<UserAccount> {
    return userAccountRepository.findById(id)
  }

  @Cacheable(cacheNames = [Caches.USER_ACCOUNTS], key = "#id")
  fun getDto(id: Long): UserAccountDto? {
    return userAccountRepository.findById(id).orElse(null)?.let {
      UserAccountDto.fromEntity(it)
    }
  }

  @CacheEvict(cacheNames = [Caches.USER_ACCOUNTS], key = "#result.id")
  fun createUser(userAccount: UserAccount): UserAccount {
    userAccountRepository.save(userAccount)
    return userAccount
  }

  fun createUser(request: SignUpDto): UserAccount {
    dtoToEntity(request).let {
      this.createUser(it)
      return it
    }
  }

  @CacheEvict(Caches.USER_ACCOUNTS, key = "#userAccount.id")
  fun delete(userAccount: UserAccount) {
    userAccountRepository.delete(userAccount)
  }

  fun dtoToEntity(request: SignUpDto): UserAccount {
    val encodedPassword = encodePassword(request.password!!)
    return UserAccount(name = request.name, username = request.email, password = encodedPassword)
  }

  @get:Cacheable(cacheNames = [Caches.USER_ACCOUNTS], key = "'implicit'")
  val implicitUser: UserAccount
    get() {
      val username = "___implicit_user"
      return userAccountRepository.findByUsername(username).orElseGet {
        val account = UserAccount(name = "No auth user", username = username, role = UserAccount.Role.ADMIN)
        this.createUser(account)
        account
      }
    }

  fun findByThirdParty(type: String?, id: String?): Optional<UserAccount> {
    return userAccountRepository.findByThirdPartyAuthTypeAndThirdPartyAuthId(type!!, id!!)
  }

  @Transactional
  @CacheEvict(cacheNames = [Caches.USER_ACCOUNTS], key = "#result.id")
  fun setResetPasswordCode(userAccount: UserAccount, code: String?): UserAccount {
    val bCryptPasswordEncoder = BCryptPasswordEncoder()
    userAccount.resetPasswordCode = bCryptPasswordEncoder.encode(code)
    return userAccountRepository.save(userAccount)
  }

  @Transactional
  @CacheEvict(cacheNames = [Caches.USER_ACCOUNTS], key = "#result.id")
  fun setUserPassword(userAccount: UserAccount, password: String?): UserAccount {
    val bCryptPasswordEncoder = BCryptPasswordEncoder()
    userAccount.password = bCryptPasswordEncoder.encode(password)
    return userAccountRepository.save(userAccount)
  }

  @Transactional
  fun isResetCodeValid(userAccount: UserAccount, code: String?): Boolean {
    val bCryptPasswordEncoder = BCryptPasswordEncoder()
    return bCryptPasswordEncoder.matches(code, userAccount.resetPasswordCode)
  }

  @Transactional
  @CacheEvict(cacheNames = [Caches.USER_ACCOUNTS], key = "#result.id")
  fun removeResetCode(userAccount: UserAccount): UserAccount {
    userAccount.resetPasswordCode = null
    return userAccountRepository.save(userAccount)
  }

  fun getAllInOrganization(
    organizationId: Long,
    pageable: Pageable,
    search: String?
  ): Page<UserAccountWithOrganizationRoleView> {
    return userAccountRepository.getAllInOrganization(organizationId, pageable, search = search ?: "")
  }

  fun getAllInProject(
    projectId: Long,
    pageable: Pageable,
    search: String?,
    exceptUserId: Long? = null
  ): Page<UserAccountInProjectView> {
    return userAccountRepository.getAllInProject(projectId, pageable, search = search, exceptUserId)
  }

  fun encodePassword(rawPassword: String): String {
    val bCryptPasswordEncoder = BCryptPasswordEncoder()
    return bCryptPasswordEncoder.encode(rawPassword)
  }

  @Transactional
  @CacheEvict(cacheNames = [Caches.USER_ACCOUNTS], key = "#result.id")
  fun update(userAccount: UserAccount, dto: UserUpdateRequestDto): UserAccount {
    if (userAccount.username != dto.email) {
      getByUserName(dto.email).ifPresent { throw ValidationException(Message.USERNAME_ALREADY_EXISTS) }
      if (tolgeeProperties.authentication.needsEmailVerification) {
        emailVerificationService.createForUser(userAccount, dto.callbackUrl, dto.email)
      } else {
        userAccount.username = dto.email
      }
    }

    dto.password?.let {
      if (!it.isEmpty()) {
        userAccount.password = encodePassword(it)
      }
    }

    userAccount.name = dto.name
    return userAccountRepository.save(userAccount)
  }

  fun saveAll(userAccounts: Collection<UserAccount>): MutableList<UserAccount> =
    userAccountRepository.saveAll(userAccounts)

  @CacheEvict(cacheNames = [Caches.USER_ACCOUNTS], key = "#result.id")
  fun save(user: UserAccount): UserAccount {
    return userAccountRepository.save(user)
  }

  val isAnyUserAccount: Boolean
    get() = userAccountRepository.count() > 0
}
