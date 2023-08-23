/**
 * Copyright (C) 2023 Tolgee s.r.o. and contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.tolgee.security.authentication

import io.tolgee.model.UserAccount
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority

class TolgeeAuthentication(
  private val credentials: Any?,
  private val userAccount: UserAccount,
  private val details: TolgeeAuthenticationDetails?,
) : Authentication {
  override fun getName(): String {
    return userAccount.username
  }

  override fun getAuthorities(): Collection<GrantedAuthority> {
    return when (userAccount.role) {
      UserAccount.Role.USER -> listOf(SimpleGrantedAuthority("ROLE_USER"))
      UserAccount.Role.ADMIN -> listOf(
        SimpleGrantedAuthority("ROLE_USER"),
        SimpleGrantedAuthority("ROLE_ADMIN")
      )
      null -> emptyList()
    }
  }

  override fun getCredentials(): Any? {
    return credentials
  }

  override fun getDetails(): TolgeeAuthenticationDetails? {
    return details
  }

  override fun getPrincipal(): UserAccount {
    return userAccount
  }

  override fun isAuthenticated(): Boolean {
    return true
  }

  override fun setAuthenticated(isAuthenticated: Boolean) {
    throw IllegalArgumentException("Implementation is immutable")
  }
}
