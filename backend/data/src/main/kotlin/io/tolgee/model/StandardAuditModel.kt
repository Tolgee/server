package io.tolgee.model

import org.springframework.data.util.ProxyUtils
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.MappedSuperclass
import javax.persistence.SequenceGenerator

@MappedSuperclass
abstract class StandardAuditModel : AuditModel(), EntityWithId {
  @Id
  @SequenceGenerator(
    name = "sequenceGenerator",
    sequenceName = "hibernate_sequence",
    initialValue = 1000000000,
    allocationSize = 1000
  )
  @GeneratedValue(
    strategy = GenerationType.SEQUENCE,
    generator = "sequenceGenerator"
  )
  override var id: Long = 0

  @Transient
  override var disableActivityLogging = false

  override fun equals(other: Any?): Boolean {
    other ?: return false

    if (this === other) return true

    if (javaClass != ProxyUtils.getUserClass(other)) return false

    other as StandardAuditModel

    // entity is not stored yet, so ID can be null for different entities
    if (this.id == 0L && other.id == 0L) {
      return false
    }

    return this.id == other.id
  }

  override fun hashCode(): Int {
    return id.hashCode()
  }

  override fun toString() = "${this.javaClass.name}(id: $id)"
}
