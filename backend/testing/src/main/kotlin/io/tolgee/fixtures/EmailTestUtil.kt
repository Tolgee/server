package io.tolgee.fixtures

import io.tolgee.configuration.tolgee.TolgeeProperties
import io.tolgee.testing.assertions.Assertions
import jakarta.mail.internet.MimeMessage
import jakarta.mail.internet.MimeMultipart
import org.assertj.core.api.AbstractStringAssert
import org.mockito.Mockito
import org.mockito.kotlin.KArgumentCaptor
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.JavaMailSenderImpl
import org.springframework.stereotype.Component

@Component
class EmailTestUtil() {
  @Autowired
  @MockBean
  lateinit var javaMailSender: JavaMailSender

  @Autowired
  lateinit var tolgeeProperties: TolgeeProperties

  lateinit var messageArgumentCaptor: KArgumentCaptor<MimeMessage>

  fun initMocks() {
    messageArgumentCaptor = argumentCaptor()
    Mockito.clearInvocations(javaMailSender)
    tolgeeProperties.smtp.from = "aaa@a.a"
    whenever(javaMailSender.createMimeMessage()).thenReturn(JavaMailSenderImpl().createMimeMessage())
    whenever(javaMailSender.send(messageArgumentCaptor.capture())).thenAnswer { }
  }

  val firstMessageContent: String
    get() = messageContents.first()

  val messageContents: List<String>
    get() =
      messageArgumentCaptor.allValues.map {
        (
          (it.content as MimeMultipart)
            .getBodyPart(0).content as MimeMultipart
        )
          .getBodyPart(0).content as String
      }

  fun verifyEmailSent() {
    verify(javaMailSender).send(any<MimeMessage>())
  }

  val assertEmailTo: AbstractStringAssert<*>
    get() {
      return Assertions.assertThat(messageArgumentCaptor.firstValue.getHeader("To")[0] as String)
    }
}
