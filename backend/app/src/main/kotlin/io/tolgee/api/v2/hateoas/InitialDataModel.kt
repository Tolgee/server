package io.tolgee.api.v2.hateoas

import io.tolgee.api.v2.hateoas.organization.OrganizationModel
import io.tolgee.api.v2.hateoas.user_account.PrivateUserAccountModel
import io.tolgee.configuration.PublicConfigurationDTO

class InitialDataModel(
  val serverConfiguration: PublicConfigurationDTO,
  var userInfo: PrivateUserAccountModel? = null,
  var preferredOrganization: OrganizationModel? = null,
  var languageTag: String? = null
)
