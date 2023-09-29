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

import io.tolgee.configuration.tolgee.AuthenticationProperties
import io.tolgee.dtos.cacheable.UserAccountDto
import io.tolgee.service.security.UserAccountService
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Component
class AuthenticationDisabledFilter(
  private val authenticationProperties: AuthenticationProperties,
  private val userAccountService: UserAccountService,
) : OncePerRequestFilter() {
  private val initialUser by lazy {
    val account = userAccountService.findInitialUser()
      ?: throw IllegalStateException("Initial user does not exists")
    UserAccountDto.fromEntity(account)
  }

  override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, filterChain: FilterChain) {
    // Set the initial user as current user, always.
    SecurityContextHolder.getContext().authentication = TolgeeAuthentication(
      null,
      initialUser,
      TolgeeAuthenticationDetails(true)
    )

    filterChain.doFilter(request, response)
  }

  override fun shouldNotFilter(request: HttpServletRequest): Boolean {
    return authenticationProperties.enabled || request.method == "OPTIONS"
  }
}
