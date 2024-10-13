package io.tolgee.ee.service

import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.jwk.source.JWKSource
import com.nimbusds.jose.jwk.source.RemoteJWKSet
import com.nimbusds.jose.proc.JWSAlgorithmFamilyJWSKeySelector
import com.nimbusds.jose.proc.SecurityContext
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor
import com.posthog.java.shaded.org.json.JSONObject
import io.tolgee.constants.Message
import io.tolgee.ee.data.GenericUserResponse
import io.tolgee.ee.data.OAuth2TokenResponse
import io.tolgee.ee.exceptions.OAuthAuthorizationException
import io.tolgee.ee.model.SsoTenant
import io.tolgee.exceptions.AuthenticationException
import io.tolgee.security.authentication.JwtService
import io.tolgee.security.payload.JwtAuthenticationResponse
import io.tolgee.security.thirdParty.OAuthUserHandler
import io.tolgee.security.thirdParty.data.OAuthUserDetails
import io.tolgee.util.Logging
import io.tolgee.util.logger
import org.springframework.http.*
import org.springframework.stereotype.Service
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate
import java.net.URL
import java.util.*

@Service
class OAuthService(
  private val jwtService: JwtService,
  private val restTemplate: RestTemplate,
  private val jwtProcessor: ConfigurableJWTProcessor<SecurityContext>,
  private val tenantService: TenantService,
  private val oAuthUserHandler: OAuthUserHandler,
) : Logging {
  fun handleOAuthCallback(
    registrationId: String,
    code: String,
    redirectUrl: String,
    error: String,
    errorDescription: String,
    invitationCode: String?,
  ): JwtAuthenticationResponse? {
    if (error.isNotBlank()) {
      logger.info("Third party auth failed: $errorDescription $error")
      throw OAuthAuthorizationException(
        Message.SSO_THIRD_PARTY_AUTH_FAILED,
        "$errorDescription $error",
      )
    }

    val tenant = tenantService.getByDomain(registrationId)

    val tokenResponse =
      exchangeCodeForToken(tenant, code, redirectUrl)
        ?: throw OAuthAuthorizationException(
          Message.SSO_TOKEN_EXCHANGE_FAILED,
          null,
        )

    val userInfo = verifyAndDecodeIdToken(tokenResponse.id_token, tenant.jwkSetUri)
    return register(userInfo, tenant, invitationCode)
  }

  fun exchangeCodeForToken(
    tenant: SsoTenant,
    code: String,
    redirectUrl: String,
  ): OAuth2TokenResponse? {
    val headers =
      HttpHeaders().apply {
        contentType = MediaType.APPLICATION_FORM_URLENCODED
      }

    val body: MultiValueMap<String, String> = LinkedMultiValueMap()
    body.add("grant_type", "authorization_code")
    body.add("code", code)
    body.add("redirect_uri", redirectUrl)
    body.add("client_id", tenant.clientId)
    body.add("client_secret", tenant.clientSecret)
    body.add("scope", "openid")

    val request = HttpEntity(body, headers)
    return try {
      val response: ResponseEntity<OAuth2TokenResponse> =
        restTemplate.exchange(
          tenant.tokenUri,
          HttpMethod.POST,
          request,
          OAuth2TokenResponse::class.java,
        )
      response.body
    } catch (e: HttpClientErrorException) {
      logger.info("Failed to exchange code for token: ${e.message}")
      null // todo throw exception
    }
  }

  fun verifyAndDecodeIdToken(
    idToken: String,
    jwkSetUri: String,
  ): GenericUserResponse {
    try {
      val signedJWT = SignedJWT.parse(idToken)

      val jwkSource: JWKSource<SecurityContext> = RemoteJWKSet(URL(jwkSetUri))

      val keySelector = JWSAlgorithmFamilyJWSKeySelector(JWSAlgorithm.Family.RSA, jwkSource)
      jwtProcessor.jwsKeySelector = keySelector

      val jwtClaimsSet: JWTClaimsSet = jwtProcessor.process(signedJWT, null)

      val expirationTime: Date = jwtClaimsSet.expirationTime
      if (expirationTime.before(Date())) {
        throw OAuthAuthorizationException(Message.SSO_ID_TOKEN_EXPIRED, null)
      }

      return GenericUserResponse().apply {
        sub = jwtClaimsSet.subject
        name = jwtClaimsSet.getStringClaim("name")
        given_name = jwtClaimsSet.getStringClaim("given_name")
        family_name = jwtClaimsSet.getStringClaim("family_name")
        email = jwtClaimsSet.getStringClaim("email")
      }
    } catch (e: Exception) {
      logger.info(e.stackTraceToString())
      throw OAuthAuthorizationException(Message.SSO_USER_INFO_RETRIEVAL_FAILED, null)
    }
  }

  fun decodeJwt(jwt: String): JSONObject {
    val parts = jwt.split(".")
    if (parts.size != 3) throw IllegalArgumentException("JWT does not have 3 parts") // todo change exception type

    val payload = parts[1]
    val decodedPayload = String(Base64.getUrlDecoder().decode(payload))

    return JSONObject(decodedPayload)
  }

  private fun register(
    userResponse: GenericUserResponse,
    tenant: SsoTenant,
    invitationCode: String?,
  ): JwtAuthenticationResponse {
    val email =
      userResponse.email ?: let {
        logger.info("Third party user email is null. Missing scope email?")
        throw AuthenticationException(Message.THIRD_PARTY_AUTH_NO_EMAIL)
      }
    val userData =
      OAuthUserDetails(
        sub = userResponse.sub!!,
        name = userResponse.name,
        givenName = userResponse.given_name,
        familyName = userResponse.family_name,
        email = email,
        domain = tenant.domain,
        organizationId = tenant.organizationId,
      )
    val user = oAuthUserHandler.findOrCreateUser(userData, invitationCode, "sso")
    val jwt = jwtService.emitToken(user.id)
    return JwtAuthenticationResponse(jwt)
  }
}