package io.tolgee.ee.utils

import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.crypto.MACSigner
import com.nimbusds.jose.proc.SecurityContext
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor
import io.tolgee.ee.data.OAuth2TokenResponse
import io.tolgee.ee.service.TenantService
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.isNull
import org.mockito.kotlin.whenever
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.MvcResult
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.web.client.RestTemplate
import java.util.*

class OAuthMultiTenantsMocks(
  private var authMvc: MockMvc? = null,
  private val restTemplate: RestTemplate? = null,
  private val tenantService: TenantService? = null,
  private val jwtProcessor: ConfigurableJWTProcessor<SecurityContext>?,
) {
  companion object {
    val defaultToken =
      OAuth2TokenResponse(id_token = generateTestJwt(), scope = "scope", refresh_token = "refresh_token")

    val defaultTokenResponse =
      ResponseEntity(
        defaultToken,
        HttpStatus.OK,
      )

    val jwtClaimsSet: JWTClaimsSet
      get() {
        val claimsSet =
          JWTClaimsSet
            .Builder()
            .subject("testSubject")
            .issuer("https://test-oauth-provider.com")
            .expirationTime(Date(System.currentTimeMillis() + 3600 * 1000)) // Время действия 1 час
            .claim("name", "Test User")
            .claim("given_name", "Test")
            .claim("given_name", "Test")
            .claim("family_name", "User")
            .claim("email", "mail@mail.com")
            .build()
        return claimsSet
      }

    val jwtClaimsSet2: JWTClaimsSet
      get() {
        val claimsSet =
          JWTClaimsSet
            .Builder()
            .subject("testSubject")
            .issuer("https://test-oauth-provider.com")
            .expirationTime(Date(System.currentTimeMillis() + 3600 * 1000)) // Время действия 1 час
            .claim("name", "Test User2")
            .claim("given_name", "Test2")
            .claim("given_name", "Test2")
            .claim("family_name", "User2")
            .claim("email", "mai2@mail.com")
            .build()
        return claimsSet
      }

    private fun generateTestJwt(): String {
      val header = JWSHeader(JWSAlgorithm.HS256)

      val signedJwt = SignedJWT(header, jwtClaimsSet)

      val testSecret = "test-256-bit-secretAAAAAAAAAAAAAAA"
      val signer = MACSigner(testSecret.toByteArray())

      signedJwt.sign(signer)

      return signedJwt.serialize()
    }
  }

  fun authorize(
    registrationId: String,
    tokenResponse: ResponseEntity<OAuth2TokenResponse>? = defaultTokenResponse,
    jwtClaims: JWTClaimsSet = jwtClaimsSet,
  ): MvcResult {
    val receivedCode = "fake_access_token"
    val tenant = tenantService?.getByDomain(registrationId)!!
    // mock token exchange
    whenever(
      restTemplate?.exchange(
        eq(tenant.tokenUri),
        eq(HttpMethod.POST),
        any(),
        eq(OAuth2TokenResponse::class.java),
      ),
    ).thenReturn(tokenResponse)

    // mock parsing of jwt
    mockJwt(jwtClaims)

    return authMvc!!
      .perform(
        MockMvcRequestBuilders.get(
          "/v2/public/oauth2/callback/$registrationId?code=$receivedCode&redirect_uri=redirect_uri",
        ),
      ).andReturn()
  }

  fun getAuthLink(registrationId: String): MvcResult =
    authMvc!!
      .perform(
        MockMvcRequestBuilders
          .post("/v2/public/oauth2/callback/get-authentication-url")
          .contentType(MediaType.APPLICATION_JSON)
          .content(
            """
            {
                "domain": "$registrationId",
                "state": "state"
            }
            """.trimIndent(),
          ),
      ).andReturn()

  private fun mockJwt(jwtClaims: JWTClaimsSet) {
    whenever(
      jwtProcessor?.process(
        any<SignedJWT>(),
        isNull(),
      ),
    ).thenReturn(jwtClaims)
  }

  fun mockTokenExchange(
    tokenUri: String,
    tokenResponse: ResponseEntity<OAuth2TokenResponse>? = defaultTokenResponse,
  ) {
    whenever(
      restTemplate?.exchange(
        eq(tokenUri),
        eq(HttpMethod.POST),
        any(),
        eq(OAuth2TokenResponse::class.java),
      ),
    ).thenReturn(tokenResponse)
  }
}
