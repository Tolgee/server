package io.tolgee.ee

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.nimbusds.jose.proc.SecurityContext
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor
import io.tolgee.constants.Message
import io.tolgee.development.testDataBuilder.data.OAuthTestData
import io.tolgee.ee.data.OAuth2TokenResponse
import io.tolgee.ee.model.SsoTenant
import io.tolgee.ee.service.OAuthService
import io.tolgee.ee.service.TenantService
import io.tolgee.ee.utils.OAuthMultiTenantsMocks
import io.tolgee.exceptions.NotFoundException
import io.tolgee.model.enums.OrganizationRoleType
import io.tolgee.testing.AbstractControllerTest
import io.tolgee.testing.assertions.Assertions.assertThat
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.ResponseEntity
import org.springframework.test.web.servlet.MockMvc
import org.springframework.web.client.RestTemplate

class OAuthTest : AbstractControllerTest() {
  private lateinit var testData: OAuthTestData

  @MockBean
  @Autowired
  private val restTemplate: RestTemplate? = null

  @Autowired
  private var authMvc: MockMvc? = null

  @MockBean
  @Autowired
  private val jwtProcessor: ConfigurableJWTProcessor<SecurityContext>? = null

  @Autowired
  private lateinit var oAuthService: OAuthService

  @Autowired
  private lateinit var tenantService: TenantService

  private val oAuthMultiTenantsMocks: OAuthMultiTenantsMocks by lazy {
    OAuthMultiTenantsMocks(authMvc, restTemplate, tenantService, jwtProcessor)
  }

  @BeforeEach
  fun setup() {
    testData = OAuthTestData()
    testDataService.saveTestData(testData.root)
  }

  private fun addTenant(): SsoTenant =
    tenantService.save(
      SsoTenant().apply {
        name = "tenant1"
        domain = "registrationId"
        clientId = "clientId"
        clientSecret = "clientSecret"
        authorizationUri = "authorizationUri"
        jwkSetUri = "http://jwkSetUri"
        tokenUri = "http://tokenUri"
        redirectUriBase = "redirectUriBase"
        organizationId = testData.organization.id
      },
    )

  @Test
  fun `creates new user account and return access token on sso log in`() {
    addTenant()
    val response = oAuthMultiTenantsMocks.authorize("registrationId")
    assertThat(response.response.status).isEqualTo(200)
    val result = jacksonObjectMapper().readValue(response.response.contentAsString, HashMap::class.java)
    Assertions.assertThat(result["accessToken"]).isNotNull
    Assertions.assertThat(result["tokenType"]).isEqualTo("Bearer")
    val userName = OAuthMultiTenantsMocks.jwtClaimsSet.getStringClaim("email")
    assertThat(userAccountService.get(userName)).isNotNull
  }

  @Test
  fun `does not return auth link when tenant is disabled`() {
    val tenant = addTenant()
    tenant.isEnabledForThisOrganization = false
    tenantService.save(tenant)
    val response = oAuthMultiTenantsMocks.getAuthLink("registrationId").response
    assertThat(response.status).isEqualTo(400)
    assertThat(response.contentAsString).contains(Message.SSO_DOMAIN_NOT_ENABLED.code)
  }

  @Test
  fun `new user belongs to organization associated with the sso issuer`() {
    addTenant()
    oAuthMultiTenantsMocks.authorize("registrationId")
    val userName = OAuthMultiTenantsMocks.jwtClaimsSet.getStringClaim("email")
    val user = userAccountService.get(userName)
    assertThat(organizationRoleService.isUserOfRole(user.id, testData.organization.id, OrganizationRoleType.MEMBER))
      .isEqualTo(true)
  }

  @Test
  fun `doesn't authorize user when token exchange fails`() {
    addTenant()
    val response =
      oAuthMultiTenantsMocks.authorize(
        "registrationId",
        ResponseEntity<OAuth2TokenResponse>(null, null, 400),
      )
    assertThat(response.response.status).isEqualTo(400)
    assertThat(response.response.contentAsString).contains(Message.SSO_TOKEN_EXCHANGE_FAILED.code)
    val userName = OAuthMultiTenantsMocks.jwtClaimsSet.getStringClaim("email")
    assertThrows<NotFoundException> { userAccountService.get(userName) }
  }
}