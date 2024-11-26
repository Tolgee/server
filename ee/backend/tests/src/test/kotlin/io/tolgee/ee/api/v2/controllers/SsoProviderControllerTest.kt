package io.tolgee.ee.api.v2.controllers

import io.tolgee.constants.Feature
import io.tolgee.development.testDataBuilder.data.SsoTestData
import io.tolgee.ee.component.PublicEnabledFeaturesProvider
import io.tolgee.fixtures.andAssertThatJson
import io.tolgee.fixtures.andIsBadRequest
import io.tolgee.fixtures.andIsForbidden
import io.tolgee.fixtures.andIsOk
import io.tolgee.testing.AuthorizedControllerTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class SsoProviderControllerTest : AuthorizedControllerTest() {
  private lateinit var testData: SsoTestData

  @Autowired
  private lateinit var enabledFeaturesProvider: PublicEnabledFeaturesProvider

  @BeforeEach
  fun setup() {
    testData = SsoTestData()
    testDataService.saveTestData(testData.root)
    this.userAccount = testData.user
    enabledFeaturesProvider.forceEnabled = setOf(Feature.SSO)
  }

  @Test
  fun `creates and returns sso provider`() {
    performAuthPut(
      "/v2/organizations/${testData.organization.id}/sso",
      requestTenant(),
    ).andIsOk

    performAuthGet("/v2/organizations/${testData.organization.id}/sso")
      .andIsOk
      .andAssertThatJson {
        node("domain").isEqualTo("google")
        node("clientId").isEqualTo("dummy_client_id")
        node("clientSecret").isEqualTo("clientSecret")
        node("authorizationUri").isEqualTo("https://dummy-url.com")
        node("tokenUri").isEqualTo("tokenUri")
        // node("jwkSetUri").isEqualTo("jwkSetUri")
        node("enabled").isEqualTo(true)
      }
  }

  @Test
  fun `always creates sso provider when disabled`() {
    performAuthPut(
      "/v2/organizations/${testData.organization.id}/sso",
      requestTenant2(),
    ).andIsOk

    performAuthGet("/v2/organizations/${testData.organization.id}/sso")
      .andIsOk
      .andAssertThatJson {
        node("domain").isEqualTo("")
        node("clientId").isEqualTo("")
        node("clientSecret").isEqualTo("")
        node("authorizationUri").isEqualTo("")
        node("tokenUri").isEqualTo("")
        // node("jwkSetUri").isEqualTo("")
        node("enabled").isEqualTo(false)
      }
  }

  @Test
  fun `does not allow to save invalid sso provider #1`() {
    performAuthPut(
      "/v2/organizations/${testData.organization.id}/sso",
      requestTenantInvalid1(),
    ).andIsBadRequest
  }

  @Test
  fun `does not allow to save invalid sso provider #2`() {
    performAuthPut(
      "/v2/organizations/${testData.organization.id}/sso",
      requestTenantInvalid2(),
    ).andIsBadRequest
  }

  @Test
  fun `does not allow to save invalid sso provider #3`() {
    performAuthPut(
      "/v2/organizations/${testData.organization.id}/sso",
      requestTenantInvalid3(),
    ).andIsBadRequest
  }

  @Test
  fun `does not allow to save invalid sso provider #4`() {
    performAuthPut(
      "/v2/organizations/${testData.organization.id}/sso",
      requestTenantInvalid4(),
    ).andIsBadRequest
  }

  @Test
  fun `fails if user is not owner of organization`() {
    this.userAccount = testData.userNotOwner
    loginAsUser(testData.userNotOwner.username)
    performAuthPut(
      "/v2/organizations/${testData.organization.id}/sso",
      requestTenant(),
    ).andIsForbidden
  }

  fun requestTenant() =
    mapOf(
      "domain" to "google",
      "clientId" to "dummy_client_id",
      "clientSecret" to "clientSecret",
      "authorizationUri" to "https://dummy-url.com",
      "redirectUri" to "redirectUri",
      "tokenUri" to "tokenUri",
      // "jwkSetUri" to "jwkSetUri",
      "enabled" to true,
    )

  fun requestTenant2() =
    mapOf(
      "domain" to "",
      "clientId" to "",
      "clientSecret" to "",
      "authorizationUri" to "",
      "redirectUri" to "",
      "tokenUri" to "",
      // "jwkSetUri" to "",
      "enabled" to false,
    )

  fun requestTenantInvalid1() =
    mapOf(
      "domain" to "",
      "clientId" to "dummy_client_id",
      "clientSecret" to "clientSecret",
      "authorizationUri" to "https://dummy-url.com",
      "redirectUri" to "redirectUri",
      "tokenUri" to "tokenUri",
      // "jwkSetUri" to "jwkSetUri",
      "enabled" to true,
    )

  fun requestTenantInvalid2() =
    mapOf(
      "domain" to "",
      "clientId" to "",
      "clientSecret" to "",
      "authorizationUri" to "",
      "redirectUri" to "",
      "tokenUri" to "",
      // "jwkSetUri" to "",
      "enabled" to true,
    )

  fun requestTenantInvalid3() =
    mapOf(
      "enabled" to true,
    )

  fun requestTenantInvalid4() =
    mapOf(
      "domain" to "asdfasdf00".repeat(26),
      "clientId" to "dummy_client_id",
      "clientSecret" to "clientSecret",
      "authorizationUri" to "https://dummy-url.com",
      "redirectUri" to "redirectUri",
      "tokenUri" to "tokenUri",
      // "jwkSetUri" to "jwkSetUri",
      "enabled" to true,
    )
}
