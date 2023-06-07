package io.tolgee.api.v2.controllers

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import io.tolgee.dtos.request.UserMfaRecoveryRequestDto
import io.tolgee.dtos.request.UserTotpDisableRequestDto
import io.tolgee.dtos.request.UserTotpEnableRequestDto
import io.tolgee.security.AuthenticationFacade
import io.tolgee.security.JwtTokenProvider
import io.tolgee.security.patAuth.DenyPatAccess
import io.tolgee.security.payload.JwtAuthenticationResponse
import io.tolgee.service.security.MfaService
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.validation.Valid

@RestController
@RequestMapping("/v2/user/mfa")
@Tag(name = "User Multi-Factor Authentication")
class UserMfaController(
  private val authenticationFacade: AuthenticationFacade,
  private val mfaService: MfaService,
  private val jwtTokenProvider: JwtTokenProvider
) {
  @PutMapping("/totp")
  @Operation(summary = "Enables TOTP-based two-factor authentication. Invalidates all previous sessions upon success.")
  @DenyPatAccess
  fun enableMfa(@RequestBody @Valid dto: UserTotpEnableRequestDto): JwtAuthenticationResponse {
    mfaService.enableTotpFor(authenticationFacade.userAccountEntity, dto)
    return JwtAuthenticationResponse(
      jwtTokenProvider.generateToken(authenticationFacade.userAccountEntity.id).toString()
    )
  }

  @DeleteMapping("/totp")
  @Operation(summary = "Disables TOTP-based two-factor authentication. Invalidates all previous sessions upon success.")
  @DenyPatAccess
  fun disableMfa(@RequestBody @Valid dto: UserTotpDisableRequestDto): JwtAuthenticationResponse {
    mfaService.disableTotpFor(authenticationFacade.userAccountEntity, dto)
    return JwtAuthenticationResponse(
      jwtTokenProvider.generateToken(authenticationFacade.userAccountEntity.id).toString()
    )
  }

  @PutMapping("/recovery")
  @Operation(summary = "Regenerates multi-factor authentication recovery codes")
  @DenyPatAccess
  fun regenerateRecoveryCodes(@RequestBody @Valid dto: UserMfaRecoveryRequestDto): List<String> {
    return mfaService.regenerateRecoveryCodes(authenticationFacade.userAccountEntity, dto)
  }
}
