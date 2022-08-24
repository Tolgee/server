package io.tolgee.controllers

import io.tolgee.configuration.tolgee.AuthenticationProperties
import io.tolgee.configuration.tolgee.TolgeeProperties
import io.tolgee.dtos.request.UserUpdateRequestDto
import io.tolgee.fixtures.JavaMailSenderMocked
import io.tolgee.fixtures.andIsOk
import io.tolgee.testing.AuthorizedControllerTest
import io.tolgee.testing.ContextRecreatingTest
import io.tolgee.testing.assertions.Assertions.assertThat
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import javax.mail.internet.MimeMessage

@ContextRecreatingTest
@SpringBootTest(
  properties = [
    "tolgee.front-end-url=https://fake.frontend.url"
  ]
)
class UserControllerTest : AuthorizedControllerTest(), JavaMailSenderMocked {

  @MockBean
  @Autowired
  override lateinit var javaMailSender: JavaMailSender

  @Autowired
  override lateinit var tolgeeProperties: TolgeeProperties

  @Autowired
  lateinit var authenticationProperties: AuthenticationProperties

  @Autowired
  lateinit var passwordEncoder: PasswordEncoder

  override lateinit var messageArgumentCaptor: ArgumentCaptor<MimeMessage>

  @Test
  fun updateUser() {
    val requestDTO = UserUpdateRequestDto(
      email = "ben@ben.aa",
      password = "super new password",
      name = "Ben's new name",
      currentPassword = initialPassword
    )
    performAuthPost("/api/user", requestDTO).andExpect(MockMvcResultMatchers.status().isOk)
    val fromDb = userAccountService.findOptional(requestDTO.email)
    Assertions.assertThat(fromDb).isNotEmpty
    Assertions.assertThat(passwordEncoder.matches(requestDTO.password, fromDb.get().password))
      .describedAs("Password is changed").isTrue
    Assertions.assertThat(fromDb.get().name).isEqualTo(requestDTO.name)
  }

  @Test
  fun updateUserValidation() {
    var requestDTO = UserUpdateRequestDto(
      email = "ben@ben.aa",
      password = "",
      name = "",
      currentPassword = initialPassword
    )
    var mvcResult = performAuthPost("/api/user", requestDTO)
      .andExpect(MockMvcResultMatchers.status().isBadRequest).andReturn()
    val standardValidation = assertThat(mvcResult).error().isStandardValidation
    standardValidation.onField("password")
    standardValidation.onField("name")

    requestDTO = UserUpdateRequestDto(
      email = "ben@ben.aa",
      password = "aksjhd  dasdsa",
      name = "a",
      currentPassword = initialPassword
    )
    dbPopulator.createUserIfNotExists(requestDTO.email)
    mvcResult = performAuthPost("/api/user", requestDTO)
      .andExpect(MockMvcResultMatchers.status().isBadRequest).andReturn()
    assertThat(mvcResult)
      .error().isCustomValidation.hasMessage("username_already_exists")
  }

  @Test
  fun updateUserSendsEmail() {
    val oldNeedsVerification = tolgeeProperties.authentication.needsEmailVerification
    tolgeeProperties.authentication.needsEmailVerification = true

    val requestDTO = UserUpdateRequestDto(
      email = "ben@ben.aaa",
      name = "Ben Ben",
      currentPassword = initialPassword
    )
    performAuthPost("/api/user", requestDTO).andIsOk

    verify(javaMailSender).send(messageArgumentCaptor.capture())
    assertThat(messageArgumentCaptor.value.tolgeeStandardMessageContent)
      .contains(tolgeeProperties.frontEndUrl.toString())

    tolgeeProperties.authentication.needsEmailVerification = oldNeedsVerification
  }

  @Test
  fun updateEmailWithoutPassword() {
    loginAsUser(dbPopulator.createUserIfNotExists("ben@ben.aa"))
    val requestDTO = UserUpdateRequestDto(name = "a", email = "ben@ben.zz")
    performAuthPost("/api/user", requestDTO).andExpect(MockMvcResultMatchers.status().isUnauthorized)
  }

  @Test
  fun updatePasswordWithoutPassword() {
    loginAsUser(dbPopulator.createUserIfNotExists("ben@ben.aa"))
    val requestDTO = UserUpdateRequestDto(name = "a", email = "ben@ben.aa", password = "super new password")
    performAuthPost("/api/user", requestDTO).andExpect(MockMvcResultMatchers.status().isUnauthorized)
  }

  @Test
  fun updateNameWithoutPassword() {
    loginAsUser(dbPopulator.createUserIfNotExists("ben@ben.aa"))
    val requestDTO = UserUpdateRequestDto(name = "zzz", email = "ben@ben.aa")
    performAuthPost("/api/user", requestDTO).andExpect(MockMvcResultMatchers.status().isOk)
  }
}
