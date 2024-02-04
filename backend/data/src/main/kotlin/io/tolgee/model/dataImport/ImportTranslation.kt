package io.tolgee.model.dataImport

import io.tolgee.model.StandardAuditModel
import io.tolgee.model.translation.Translation
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.ManyToOne
import jakarta.validation.constraints.NotNull
import org.apache.commons.codec.digest.MurmurHash3
import org.hibernate.annotations.ColumnDefault
import java.nio.ByteBuffer
import java.util.*

@Entity
class ImportTranslation(
  @Column(columnDefinition = "text")
  var text: String?,
  @ManyToOne
  var language: ImportLanguage,
) : StandardAuditModel() {
  @ManyToOne(optional = false)
  lateinit var key: ImportKey

  @ManyToOne
  var conflict: Translation? = null

  /**
   * Whether this translation will override the conflict
   */
  @field:NotNull
  var override: Boolean = false

  /**
   * Whether user explicitely resolved this conflict
   */
  val resolved: Boolean
    get() = this.conflict?.text.computeMurmur() == this.resolvedHash

  /**
   * If user resolved the conflict, this field stores hash of existing translation text
   * This field is then used to check whether the translation was not changed in meantime
   */
  @Column
  var resolvedHash: String? = null

  fun resolve() {
    resolvedHash = conflict?.text.computeMurmur()
  }

  /**
   * If user selects the same language for multiple files, there can be conflicts between translations
   * of same language and key. This field is used to select which translation should be imported
   */
  @ColumnDefault("true")
  var isSelectedToImport: Boolean = true

  private fun String?.computeMurmur(): String? {
    if (this == null) {
      return "__null_value"
    }
    val hash =
      MurmurHash3.hash128(this.toByteArray()).asSequence().flatMap {
        val buffer = ByteBuffer.allocate(java.lang.Long.BYTES)
        buffer.putLong(it)
        buffer.array().asSequence()
      }.toList().toByteArray()
    return Base64.getEncoder().encodeToString(hash)
  }
}
