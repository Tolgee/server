package io.tolgee.development.testDataBuilder.data

import io.tolgee.development.testDataBuilder.builders.UserAccountBuilder
import io.tolgee.model.enums.OrganizationRoleType

class SsoOrganizationsLoginTestData : BaseTestData("ssoOrgLoginTestUser", "Empty project") {
  var orgAdmin: UserAccountBuilder
//  var orgMember: UserAccountBuilder

  init {
//    orgMember =
//      root.addUserAccount {
//        username = "organization.member@test.com"
//        name = "Organization member"
//      }

    orgAdmin =
      root.addUserAccount {
        username = "organization.owner@test.com"
        name = "Organization owner"
      }

    userAccountBuilder.defaultOrganizationBuilder.apply {
//      addRole {
//        user = orgMember.self
//        type = OrganizationRoleType.MEMBER
//      }

      addRole {
        user = orgAdmin.self
        type = OrganizationRoleType.OWNER
      }

      setTenant {
        enabled = true
        authorizationUri = "https://dummy-url.com"
        clientId = "dummy_client_id"
        clientSecret = "clientSecret"
        domain = "domain.com"
        jwkSetUri = "http://jwkSetUri"
        tokenUri = "http://tokenUri"
      }
    }
  }
}
