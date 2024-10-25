package io.tolgee.security.thirdParty

import io.tolgee.component.cacheWithExpiration.CacheWithExpirationManager
import io.tolgee.constants.Caches
import io.tolgee.constants.Message
import io.tolgee.exceptions.AuthenticationException
import io.tolgee.model.UserAccount
import io.tolgee.model.enums.OrganizationRoleType
import io.tolgee.security.thirdParty.data.OAuthUserDetails
import io.tolgee.service.organization.OrganizationRoleService
import io.tolgee.service.security.SignUpService
import io.tolgee.service.security.UserAccountService
import org.springframework.stereotype.Component

@Component
class OAuthUserHandler(
  private val signUpService: SignUpService,
  private val organizationRoleService: OrganizationRoleService,
  private val userAccountService: UserAccountService,
  private val cacheWithExpirationManager: CacheWithExpirationManager,
) {
  fun findOrCreateUser(
    userResponse: OAuthUserDetails,
    invitationCode: String?,
    thirdPartyAuthType: String,
  ): UserAccount {
    val userAccountOptional =
      if (thirdPartyAuthType == "sso" && userResponse.domain != null) {
        userAccountService.findByDomainSso(userResponse.domain, userResponse.sub!!)
      } else {
        userAccountService.findByThirdParty(thirdPartyAuthType, userResponse.sub!!)
      }

    if (userAccountOptional.isPresent && thirdPartyAuthType == "sso") {
      updateRefreshToken(userAccountOptional.get(), userResponse.refreshToken)
      cacheSsoUser(userAccountOptional.get().id, thirdPartyAuthType)
    }

    return userAccountOptional.orElseGet {
      userAccountService.findActive(userResponse.email)?.let {
        throw AuthenticationException(Message.USERNAME_ALREADY_EXISTS)
      }

      val newUserAccount = UserAccount()
      newUserAccount.username = userResponse.email

      val name =
        userResponse.name ?: run {
          if (userResponse.givenName != null && userResponse.familyName != null) {
            "${userResponse.givenName} ${userResponse.familyName}"
          } else {
            userResponse.email.split("@")[0]
          }
        }
      newUserAccount.name = name
      newUserAccount.thirdPartyAuthId = userResponse.sub
      newUserAccount.ssoDomain = userResponse.domain
      newUserAccount.thirdPartyAuthType = thirdPartyAuthType
      newUserAccount.ssoRefreshToken = userResponse.refreshToken
      newUserAccount.accountType = UserAccount.AccountType.THIRD_PARTY

      signUpService.signUp(newUserAccount, invitationCode, null)

      // grant role to user only if request is not from oauth2 delegate
      if (userResponse.organizationId != null && thirdPartyAuthType != "oauth2") {
        organizationRoleService.grantRoleToUser(
          newUserAccount,
          userResponse.organizationId,
          OrganizationRoleType.MEMBER,
        )
      }

      cacheSsoUser(newUserAccount.id, thirdPartyAuthType)

      newUserAccount
    }
  }

  private fun updateRefreshToken(
    userAccount: UserAccount,
    refreshToken: String?,
  ) {
    if (userAccount.ssoRefreshToken != refreshToken) {
      userAccount.ssoRefreshToken = refreshToken
      userAccountService.save(userAccount)
    }
  }

  private fun cacheSsoUser(
    userId: Long,
    thirdPartyAuthType: String,
  ) {
    if (thirdPartyAuthType == "sso") {
      cacheWithExpirationManager.putCache(Caches.IS_SSO_USER_VALID, userId, true)
    }
  }
}
