package io.tolgee.model.contentDelivery

import com.vladmihalcea.hibernate.type.json.JsonBinaryType
import io.tolgee.dtos.IExportParams
import io.tolgee.dtos.request.export.ExportFormat
import io.tolgee.model.Project
import io.tolgee.model.StandardAuditModel
import io.tolgee.model.automations.AutomationAction
import io.tolgee.model.enums.TranslationState
import org.hibernate.annotations.Type
import org.hibernate.annotations.TypeDef
import org.hibernate.annotations.TypeDefs
import java.util.*
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.ManyToOne
import javax.persistence.OneToMany
import javax.persistence.Table
import javax.persistence.UniqueConstraint

@Entity()
@Table(
  uniqueConstraints = [UniqueConstraint(columnNames = ["project_id", "slug"])]
)
@TypeDefs(
  value = [TypeDef(name = "jsonb", typeClass = JsonBinaryType::class)]
)
class ContentDeliveryConfig(
  @ManyToOne(fetch = FetchType.LAZY)
  var project: Project,
) : StandardAuditModel(), IExportParams {
  var name: String = ""

  var slug: String = ""

  @ManyToOne
  var contentStorage: ContentStorage? = null

  @OneToMany(mappedBy = "contentDeliveryConfig")
  var automationActions: MutableList<AutomationAction> = mutableListOf()

  var lastPublished: Date? = null

  @Type(type = "jsonb")
  override var languages: Set<String>? = null

  override var format: ExportFormat = ExportFormat.JSON
  override var structureDelimiter: Char? = '.'

  @Type(type = "jsonb")
  override var filterKeyId: List<Long>? = null

  @Type(type = "jsonb")
  override var filterKeyIdNot: List<Long>? = null
  override var filterTag: String? = null
  override var filterKeyPrefix: String? = null

  @Type(type = "jsonb")
  override var filterState: List<TranslationState>? = listOf(
    TranslationState.TRANSLATED,
    TranslationState.REVIEWED,
  )

  @Type(type = "jsonb")
  override var filterNamespace: List<String?>? = null
}
