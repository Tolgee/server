package io.tolgee.configuration.tolgee

import io.tolgee.model.cdn.S3Config
import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "tolgee.cdn.s3")
class CdnS3Properties : S3Config {
  override var bucketName: String? = null
  override var accessKey: String? = null
  override var secretKey: String? = null
  override var endpoint: String? = null
  override var signingRegion: String? = null

  fun clear() {
    bucketName = null
    accessKey = null
    secretKey = null
    endpoint = null
    signingRegion = null
  }
}
