package io.tolgee.model.batch

import com.vladmihalcea.hibernate.type.json.JsonBinaryType
import io.tolgee.model.StandardAuditModel
import org.hibernate.annotations.Type
import org.hibernate.annotations.TypeDef
import org.hibernate.annotations.TypeDefs
import javax.persistence.Entity
import javax.persistence.ManyToOne

@Entity
@TypeDefs(
  value = [TypeDef(name = "jsonb", typeClass = JsonBinaryType::class)]
)
class BatchJobChunkExecution : StandardAuditModel() {
  @ManyToOne
  lateinit var batchJob: BatchJob

  var status: BatchJobChunkExecutionStatus = BatchJobChunkExecutionStatus.FAILED

  var chunkNumber: Int = 0

  @Type(type = "jsonb")
  var successTargets: List<Long> = listOf()
}
