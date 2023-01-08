package io.tolgee.controllers

import io.tolgee.dtos.misc.CreateProjectInvitationParams
import io.tolgee.dtos.request.auth.SignUpDto
import io.tolgee.fixtures.andAssertResponse
import io.tolgee.fixtures.andIsBadRequest
import io.tolgee.fixtures.andIsOk
import io.tolgee.fixtures.generateUniqueString
import io.tolgee.model.enums.ProjectPermissionType
import io.tolgee.testing.AbstractControllerTest
import io.tolgee.testing.assertions.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
@AutoConfigureMockMvc
class PublicControllerTest :
  AbstractControllerTest() {

  @Test
  fun `creates organization`() {
    val dto = SignUpDto(
      name = "Pavel Novak",
      password = "aaaaaaaaa",
      email = "aaaa@aaaa.com",
      organizationName = "Hello"
    )
    performPost("/api/public/sign_up", dto).andIsOk
    assertThat(organizationRepository.findAllByName("Hello")).hasSize(1)
  }

  @Test
  fun `creates organization when no invitation`() {
    val dto = SignUpDto(name = "Pavel Novak", password = "aaaaaaaaa", email = "aaaa@aaaa.com")
    performPost("/api/public/sign_up", dto).andIsOk
    assertThat(organizationRepository.findAllByName("Pavel Novak")).hasSize(1)
  }

  @Test
  fun `doesn't create organization when invitation provided`() {
    val base = dbPopulator.createBase(generateUniqueString())
    val project = base.project
    val invitation = invitationService.create(
      CreateProjectInvitationParams(project, ProjectPermissionType.EDIT, null)
    )
    val dto = SignUpDto(
      name = "Pavel Novak",
      password = "aaaaaaaaa",
      email = "aaaa@aaaa.com",
      invitationCode = invitation.code
    )
    performPost("/api/public/sign_up", dto).andIsOk
    assertThat(organizationRepository.findAllByName("Pavel Novak")).hasSize(0)
  }

  @Test
  fun testSignUpValidationBlankEmail() {
    val dto = SignUpDto(name = "Pavel Novak", password = "aaaa", email = "")
    performPost("/api/public/sign_up", dto)
      .andIsBadRequest
      .andAssertResponse.error().isStandardValidation.onField("email")
  }

  @Test
  fun testSignUpValidationBlankName() {
    val dto = SignUpDto(name = "", password = "aaaa", email = "aaa@aaa.cz")
    performPost("/api/public/sign_up", dto)
      .andIsBadRequest
      .andAssertResponse.error().isStandardValidation.onField("name")
  }

  @Test
  fun testSignUpValidationInvalidEmail() {
    val dto = SignUpDto(name = "", password = "aaaa", email = "aaaaaa.cz")
    performPost("/api/public/sign_up", dto)
      .andIsBadRequest
      .andAssertResponse.error().isStandardValidation.onField("email")
  }
}
