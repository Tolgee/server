package io.tolgee.ee.api.v2.controllers

import io.tolgee.component.enabledFeaturesProvider.EnabledFeaturesProvider
import io.tolgee.constants.Feature
import io.tolgee.ee.api.v2.hateoas.assemblers.SsoTenantAssembler
import io.tolgee.ee.data.CreateProviderRequest
import io.tolgee.ee.data.toDto
import io.tolgee.ee.service.TenantService
import io.tolgee.exceptions.NotFoundException
import io.tolgee.hateoas.ee.SsoTenantModel
import io.tolgee.model.enums.OrganizationRoleType
import io.tolgee.security.OrganizationHolder
import io.tolgee.security.authentication.RequiresSuperAuthentication
import io.tolgee.security.authorization.RequiresOrganizationRole
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*

@RestController
@CrossOrigin(origins = ["*"])
@RequestMapping(value = ["/v2/organizations/{organizationId:[0-9]+}/sso"])
class SsoProviderController(
  private val tenantService: TenantService,
  private val ssoTenantAssembler: SsoTenantAssembler,
  private val enabledFeaturesProvider: EnabledFeaturesProvider,
  private val organizationHolder: OrganizationHolder,
) {
  @RequiresOrganizationRole(role = OrganizationRoleType.OWNER)
  @PutMapping("")
  @RequiresSuperAuthentication
  fun setProvider(
    @RequestBody @Valid request: CreateProviderRequest,
    @PathVariable organizationId: Long,
  ): SsoTenantModel {
    enabledFeaturesProvider.checkFeatureEnabled(
      organizationId =
        organizationHolder.organization.id,
      Feature.SSO,
    )

    return ssoTenantAssembler.toModel(tenantService.saveOrUpdate(request, organizationId).toDto())
  }

  @RequiresOrganizationRole(role = OrganizationRoleType.OWNER)
  @GetMapping("")
  @RequiresSuperAuthentication
  fun findProvider(
    @PathVariable organizationId: Long,
  ): SsoTenantModel? =
    try {
      enabledFeaturesProvider.checkFeatureEnabled(
        organizationId =
          organizationHolder.organization.id,
        Feature.SSO,
      )

      ssoTenantAssembler.toModel(tenantService.getTenant(organizationId).toDto())
    } catch (e: NotFoundException) {
      null
    }
}
