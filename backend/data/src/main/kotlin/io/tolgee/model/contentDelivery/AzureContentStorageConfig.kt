package io.tolgee.model.contentDelivery

import org.springframework.data.annotation.AccessType
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.MapsId
import javax.persistence.OneToOne
import javax.validation.constraints.NotBlank

@Entity()
class AzureContentStorageConfig(
  @MapsId
  @JoinColumn(name = "content_storage_id")
  @OneToOne(fetch = FetchType.LAZY)
  var contentStorage: ContentStorage,
) : AzureBlobConfig {
  @Id
  @AccessType(AccessType.Type.PROPERTY)
  @Column(name = "content_storage_id")
  private val id: Long? = null

  @field:NotBlank
  override var connectionString: String? = ""

  @field:NotBlank
  override var containerName: String? = ""
}
