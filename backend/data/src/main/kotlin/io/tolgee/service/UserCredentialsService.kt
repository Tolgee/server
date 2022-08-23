package io.tolgee.service

import io.tolgee.configuration.tolgee.TolgeeProperties
import io.tolgee.constants.Message
import io.tolgee.exceptions.AuthenticationException
import io.tolgee.model.UserAccount
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class UserCredentialsService @Autowired constructor(
  private val passwordEncoder: PasswordEncoder,
  private val configuration: TolgeeProperties
) {

  @set:Autowired
  lateinit var apiKeyService: ApiKeyService

  @set:Autowired
  lateinit var permissionService: PermissionService

  @set:Autowired
  lateinit var userAccountService: UserAccountService

  @set:Autowired
  lateinit var authenticationManager: AuthenticationManager

  fun checkUserCredentials(username: String, password: String): UserAccount {
    if (configuration.authentication.ldap.enabled) {
      val details = checkLdapUserCredentials(username, password)
      return userAccountService.findOptional(details.username).orElseGet {
        val userAccount = UserAccount()
        userAccount.username = details.username
        userAccountService.createUser(userAccount)
        userAccount
      }
    }

    val userAccount = userAccountService.findOptional(username).orElseThrow {
      AuthenticationException(Message.BAD_CREDENTIALS)
    }

    checkNativeUserCredentials(userAccount, password)
    return userAccount
  }

  fun checkUserCredentials(user: UserAccount, password: String) {
    if (configuration.authentication.ldap.enabled) {
      checkLdapUserCredentials(user.username, password)
    } else {
      checkNativeUserCredentials(user, password)
    }
  }

  private fun checkNativeUserCredentials(user: UserAccount, password: String) {
    if (!passwordEncoder.matches(password, user.password)) {
      throw AuthenticationException(Message.BAD_CREDENTIALS)
    }
  }

  private fun checkLdapUserCredentials(username: String, password: String): UserDetails {
    try {
      val res = authenticationManager.authenticate(UsernamePasswordAuthenticationToken(username, password))
      return res.principal as UserDetails
    } catch (e: BadCredentialsException) {
      throw AuthenticationException(Message.BAD_CREDENTIALS)
    }
  }
}
