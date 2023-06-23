package io.tolgee.model.batch

import com.vladmihalcea.hibernate.type.json.JsonBinaryType
import io.tolgee.batch.BatchJobType
import io.tolgee.model.Project
import io.tolgee.model.StandardAuditModel
import io.tolgee.model.UserAccount
import org.hibernate.annotations.Type
import org.hibernate.annotations.TypeDef
import org.hibernate.annotations.TypeDefs
import javax.persistence.Entity
import javax.persistence.Enumerated
import javax.persistence.FetchType
import javax.persistence.ManyToOne

@Entity
@TypeDefs(
  value = [TypeDef(name = "jsonb", typeClass = JsonBinaryType::class)]
)
class BatchJob : StandardAuditModel() {
  @ManyToOne(fetch = FetchType.LAZY)
  lateinit var project: Project

  @ManyToOne(fetch = FetchType.LAZY)
  var author: UserAccount? = null

  @Type(type = "jsonb")
  var target: List<Long> = listOf()

  var totalItems: Int = 0

  var totalChunks: Int = 0

  var chunkSize: Int = 0

  var status: BatchJobStatus = BatchJobStatus.PENDING

  @Enumerated
  var type: BatchJobType = BatchJobType.TRANSLATION

  val chunkedTarget get() = if (chunkSize == 0) listOf(target) else target.chunked(chunkSize)
}
