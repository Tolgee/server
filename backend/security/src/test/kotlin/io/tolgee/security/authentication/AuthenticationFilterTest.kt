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

import io.tolgee.component.CurrentDateProvider
import io.tolgee.constants.Message
import io.tolgee.exceptions.AuthenticationException
import io.tolgee.model.ApiKey
import io.tolgee.model.Pat
import io.tolgee.model.UserAccount
import io.tolgee.security.ratelimit.RateLimitPolicy
import io.tolgee.security.ratelimit.RateLimitService
import io.tolgee.security.ratelimit.RateLimitedException
import io.tolgee.service.security.ApiKeyService
import io.tolgee.service.security.PatService
import io.tolgee.testing.assertions.Assertions.assertThat
import org.checkerframework.checker.units.qual.Current
import org.junit.jupiter.api.*
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.springframework.mock.web.MockFilterChain
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.security.core.context.SecurityContextHolder
import java.util.*

class AuthenticationFilterTest {
  companion object {
    const val TEST_VALID_TOKEN = "uwu"
    const val TEST_INVALID_TOKEN = "owo"
    const val TEST_USER_ID = 1337L

    const val TEST_VALID_PAK = "tgpak_valid"
    const val TEST_INVALID_PAK = "tgpak_invalid"

    const val TEST_VALID_PAT = "tgpat_valid"
    const val TEST_INVALID_PAT = "tgpat_invalid"
  }

  private val currentDateProvider = Mockito.mock(CurrentDateProvider::class.java)

  private val authProperties = Mockito.mock(AuthenticationProperties::class.java)

  private val rateLimitService = Mockito.mock(RateLimitService::class.java)

  private val jwtService = Mockito.mock(JwtService::class.java)

  private val pakService = Mockito.mock(ApiKeyService::class.java)

  private val patService = Mockito.mock(PatService::class.java)

  private val apiKey = Mockito.mock(ApiKey::class.java)

  private val pat = Mockito.mock(Pat::class.java)

  private val userAccount = Mockito.mock(UserAccount::class.java)

  private val authenticationFilter = AuthenticationFilter(
    authProperties,
    currentDateProvider,
    rateLimitService,
    jwtService,
    pakService,
    patService,
  )

  private val authenticationFacade = AuthenticationFacade()

  @BeforeEach
  fun setupMocksAndSecurityCtx() {
    val now = Date()
    Mockito.`when`(currentDateProvider.date).thenReturn(now)

    Mockito.`when`(authProperties.enabled).thenReturn(true)

    Mockito.`when`(rateLimitService.getIpAuthRateLimitPolicy(any()))
      .thenReturn(
        RateLimitPolicy("test policy", 5, 1000, true)
      )

    Mockito.`when`(rateLimitService.consumeBucketUnless(any(), any()))
      .then {
        val fn = it.getArgument<() -> Boolean>(1)
        fn()
      }

    Mockito.`when`(jwtService.validateToken(TEST_VALID_TOKEN))
      .thenReturn(
        TolgeeAuthentication(
          "uwu",
          userAccount,
          null,
        )
      )

    Mockito.`when`(jwtService.validateToken(TEST_INVALID_TOKEN))
      .thenThrow(AuthenticationException(Message.INVALID_JWT_TOKEN))

    Mockito.`when`(pakService.hashKey(TEST_VALID_PAK)).thenReturn(TEST_VALID_PAK)
    Mockito.`when`(pakService.hashKey(TEST_INVALID_PAK)).thenReturn(TEST_INVALID_PAK)
    Mockito.`when`(pakService.find(Mockito.anyString())).thenReturn(null)
    Mockito.`when`(pakService.find(TEST_VALID_PAK)).thenReturn(apiKey)

    Mockito.`when`(patService.hashToken(TEST_VALID_PAT)).thenReturn(TEST_VALID_PAT)
    Mockito.`when`(patService.hashToken(TEST_INVALID_PAT)).thenReturn(TEST_INVALID_PAT)
    Mockito.`when`(patService.find(Mockito.anyString())).thenReturn(null)
    Mockito.`when`(patService.find(TEST_VALID_PAT)).thenReturn(pat)

    Mockito.`when`(apiKey.userAccount).thenReturn(userAccount)
    Mockito.`when`(apiKey.expiresAt).thenReturn(null)
    Mockito.`when`(pat.userAccount).thenReturn(userAccount)
    Mockito.`when`(pat.expiresAt).thenReturn(null)

    Mockito.`when`(userAccount.id).thenReturn(TEST_USER_ID)

    SecurityContextHolder.getContext().authentication = null
  }

  @AfterEach
  fun resetMocks() {
    Mockito.reset(
      currentDateProvider,
      authProperties,
      rateLimitService,
      jwtService,
      pakService,
      patService,
      apiKey,
      pat,
      userAccount,
    )
  }

  @Test
  fun `it does not filter when auth is disabled`() {
    Mockito.`when`(authProperties.enabled).thenReturn(false)
    val req = MockHttpServletRequest()
    val res = MockHttpServletResponse()
    val chain = MockFilterChain()

    assertDoesNotThrow { authenticationFilter.doFilter(req, res, chain) }

    val ctx = SecurityContextHolder.getContext()
    assertThat(ctx.authentication).isNull()
  }

  @Test
  fun `it allows request to go through with valid JWT token`() {
    val req = MockHttpServletRequest()
    val res = MockHttpServletResponse()
    val chain = MockFilterChain()

    req.addHeader("Authorization", "Bearer $TEST_VALID_TOKEN")
    assertDoesNotThrow { authenticationFilter.doFilter(req, res, chain) }

    val ctx = SecurityContextHolder.getContext()
    assertThat(ctx.authentication).isNotNull
    assertThat(authenticationFacade.authenticatedUser).isEqualTo(userAccount)
    assertThat(authenticationFacade.isApiAuthentication).isEqualTo(false)
    assertThat(authenticationFacade.isProjectApiKeyAuth).isEqualTo(false)
    assertThat(authenticationFacade.isPersonalAccessTokenAuth).isEqualTo(false)
  }

  @Test
  fun `it does not allow request to go through with invalid JWT tokens`() {
    val req = MockHttpServletRequest()
    val res = MockHttpServletResponse()
    val chain = MockFilterChain()

    req.addHeader("Authorization", "Bearer $TEST_INVALID_TOKEN")
    assertThrows<AuthenticationException> { authenticationFilter.doFilter(req, res, chain) }

    chain.reset()
    req.removeHeader("Authorization")
    req.addHeader("Authorization", TEST_VALID_TOKEN)
    assertThrows<AuthenticationException> { authenticationFilter.doFilter(req, res, chain) }
  }

  @Test
  fun `it allows request to go through when using valid PAK`() {
    val req = MockHttpServletRequest()
    val res = MockHttpServletResponse()
    val chain = MockFilterChain()

    req.addHeader("X-API-Key", TEST_VALID_PAK)
    assertDoesNotThrow { authenticationFilter.doFilter(req, res, chain) }

    val ctx = SecurityContextHolder.getContext()
    assertThat(ctx.authentication).isNotNull
    assertThat(authenticationFacade.authenticatedUser).isEqualTo(userAccount)
    assertThat(authenticationFacade.isApiAuthentication).isEqualTo(true)
    assertThat(authenticationFacade.isProjectApiKeyAuth).isEqualTo(true)
    assertThat(authenticationFacade.isPersonalAccessTokenAuth).isEqualTo(false)
    assertThat(authenticationFacade.projectApiKey).isEqualTo(apiKey)
  }

  @Test
  fun `it allows request to go through when using invalid PAK`() {
    val req = MockHttpServletRequest()
    val res = MockHttpServletResponse()
    val chain = MockFilterChain()

    req.addHeader("X-API-Key", TEST_INVALID_PAK)
    assertThrows<AuthenticationException> { authenticationFilter.doFilter(req, res, chain) }
  }

  @Test
  fun `it allows request to go through when using expired PAK`() {
    val now = currentDateProvider.date
    Mockito.`when`(apiKey.expiresAt).thenReturn(Date(now.time - 10_000))

    val req = MockHttpServletRequest()
    val res = MockHttpServletResponse()
    val chain = MockFilterChain()

    req.addHeader("X-API-Key", TEST_VALID_PAK)
    assertThrows<AuthenticationException> { authenticationFilter.doFilter(req, res, chain) }
  }

  @Test
  fun `it allows request to go through when using valid PAT`() {
    val req = MockHttpServletRequest()
    val res = MockHttpServletResponse()
    val chain = MockFilterChain()

    req.addHeader("X-API-Key", TEST_VALID_PAT)
    assertDoesNotThrow { authenticationFilter.doFilter(req, res, chain) }

    val ctx = SecurityContextHolder.getContext()
    assertThat(ctx.authentication).isNotNull
    assertThat(authenticationFacade.authenticatedUser).isEqualTo(userAccount)
    assertThat(authenticationFacade.isApiAuthentication).isEqualTo(true)
    assertThat(authenticationFacade.isProjectApiKeyAuth).isEqualTo(false)
    assertThat(authenticationFacade.isPersonalAccessTokenAuth).isEqualTo(true)
    assertThat(authenticationFacade.personalAccessToken).isEqualTo(pat)
  }

  @Test
  fun `it allows request to go through when using invalid PAT`() {
    val req = MockHttpServletRequest()
    val res = MockHttpServletResponse()
    val chain = MockFilterChain()

    req.addHeader("X-API-Key", TEST_INVALID_PAT)
    assertThrows<AuthenticationException> { authenticationFilter.doFilter(req, res, chain) }
  }

  @Test
  fun `it allows request to go through when using expired PAT`() {
    val now = currentDateProvider.date
    Mockito.`when`(pat.expiresAt).thenReturn(Date(now.time - 10_000))

    val req = MockHttpServletRequest()
    val res = MockHttpServletResponse()
    val chain = MockFilterChain()

    req.addHeader("X-API-Key", TEST_VALID_PAT)
    assertThrows<AuthenticationException> { authenticationFilter.doFilter(req, res, chain) }
  }

  @Test
  fun `it applies a rate limit on authentication attempts`() {
    val req = MockHttpServletRequest()
    val res = MockHttpServletResponse()
    val chain = MockFilterChain()

    Mockito.`when`(rateLimitService.consumeBucketUnless(any(), any()))
      .thenThrow(RateLimitedException(1000L, true))

    req.addHeader("Authorization", "Bearer $TEST_VALID_TOKEN")
    assertThrows<RateLimitedException> { authenticationFilter.doFilter(req, res, chain) }

    req.removeHeader("Authorization")
    req.addHeader("X-API-Key", TEST_VALID_PAK)
    assertThrows<RateLimitedException> { authenticationFilter.doFilter(req, res, chain) }

    req.removeHeader("X-API-Key")
    req.addHeader("X-API-Key", TEST_VALID_PAT)
    assertThrows<RateLimitedException> { authenticationFilter.doFilter(req, res, chain) }
  }
}
