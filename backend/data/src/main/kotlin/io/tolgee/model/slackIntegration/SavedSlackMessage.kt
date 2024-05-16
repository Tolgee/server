package io.tolgee.model.slackIntegration

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType
import io.tolgee.model.StandardAuditModel
import jakarta.persistence.*
import org.hibernate.annotations.Type

@Entity
class SavedSlackMessage(
  val messageTs: String = "",
  @ManyToOne(fetch = FetchType.LAZY)
  var slackConfig: SlackConfig = SlackConfig(),
  var keyId: Long = 0L,
  @Column(columnDefinition = "jsonb")
  @Type(JsonBinaryType::class)
  var langTags: Set<String> = setOf(),
  var createdKeyBlocks: Boolean = false,
) : StandardAuditModel() {
  @OneToMany(mappedBy = "slackMessage", fetch = FetchType.LAZY)
  var info: MutableList<SlackMessageInfo> = mutableListOf()
}
