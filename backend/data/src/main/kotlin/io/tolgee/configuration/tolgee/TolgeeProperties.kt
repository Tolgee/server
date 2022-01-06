/*
 * Copyright (c) 2020. Tolgee
 */

package io.tolgee.configuration.tolgee

import io.tolgee.configuration.tolgee.machineTranslation.MachineTranslationProperties
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConfigurationProperties(prefix = "tolgee")
@ConstructorBinding
open class TolgeeProperties(
  var authentication: AuthenticationProperties = AuthenticationProperties(),
  var smtp: SmtpProperties = SmtpProperties(),
  var sentry: SentryProperties = SentryProperties(),
  var internal: InternalProperties = InternalProperties(),
  var screenshotsUrl: String = "/screenshots",
  var uploadedImagesUrl: String = "/uploaded-images",
  var maxUploadFileSize: Int = 2048,
  val maxScreenshotsPerKey: Int = 20,
  var fileStorage: FileStorageProperties = FileStorageProperties(),
  var frontEndUrl: String? = null,
  var socketIo: SocketIoProperties = SocketIoProperties(),
  var appName: String = "Tolgee",
  var maxTranslationTextLength: Long = 10000,
  var cache: CacheProperties = CacheProperties(),
  var recaptcha: ReCaptchaProperties = ReCaptchaProperties(),
  var machineTranslation: MachineTranslationProperties = MachineTranslationProperties(),
  var postgresAutostart: PostgresAutostartProperties = PostgresAutostartProperties(),
  open var import: ImportProperties = ImportProperties()
)
