package io.tolgee.model

import io.hypersistence.utils.hibernate.type.array.ListArrayType
import io.tolgee.api.IUserAccount
import io.tolgee.model.notifications.NotificationPreferences
import io.tolgee.model.notifications.UserNotification
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.OneToOne
import jakarta.persistence.OrderBy
import jakarta.validation.constraints.NotBlank
import org.hibernate.annotations.ColumnDefault
import org.hibernate.annotations.Type
import org.hibernate.annotations.Where
import java.util.*

@Entity
data class UserAccount(
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  override var id: Long = 0L,
  @field:NotBlank
  var username: String = "",
  var password: String? = null,
  var name: String = "",
  @Enumerated(EnumType.STRING)
  var role: Role? = Role.USER,
  @Enumerated(EnumType.STRING)
  @Column(name = "account_type")
  override var accountType: AccountType? = AccountType.LOCAL,
) : AuditModel(), ModelWithAvatar, IUserAccount {
  @Column(name = "totp_key", columnDefinition = "bytea")
  override var totpKey: ByteArray? = null

  @Type(ListArrayType::class)
  @Column(name = "mfa_recovery_codes", columnDefinition = "text[]")
  var mfaRecoveryCodes: List<String> = emptyList()

  @Column(name = "tokens_valid_not_before")
  var tokensValidNotBefore: Date? = null

  @OneToMany(mappedBy = "user", orphanRemoval = true)
  var permissions: MutableSet<Permission> = mutableSetOf()

  @OneToOne(mappedBy = "userAccount", fetch = FetchType.LAZY, optional = true)
  var emailVerification: EmailVerification? = null

  @Column(name = "third_party_auth_type")
  var thirdPartyAuthType: String? = null

  @Column(name = "third_party_auth_id")
  var thirdPartyAuthId: String? = null

  @Column(name = "reset_password_code")
  var resetPasswordCode: String? = null

  @OrderBy("id ASC")
  @OneToMany(mappedBy = "user", orphanRemoval = true)
  var organizationRoles: MutableList<OrganizationRole> = mutableListOf()

  @OneToOne(mappedBy = "userAccount", fetch = FetchType.LAZY, cascade = [CascadeType.REMOVE], orphanRemoval = true)
  var preferences: UserPreferences? = null

  @OneToMany(mappedBy = "userAccount", orphanRemoval = true)
  var pats: MutableList<Pat>? = mutableListOf()

  @OneToMany(mappedBy = "userAccount", orphanRemoval = true)
  var apiKeys: MutableList<ApiKey>? = mutableListOf()

  override var avatarHash: String? = null

  @Column(name = "deleted_at")
  var deletedAt: Date? = null

  @Column(name = "disabled_at")
  var disabledAt: Date? = null

  @Column(name = "is_initial_user", nullable = false)
  @ColumnDefault("false")
  override var isInitialUser: Boolean = false

  @Column(name = "password_changed", nullable = false)
  @ColumnDefault("true")
  var passwordChanged: Boolean = true

  @OneToMany(fetch = FetchType.LAZY, cascade = [CascadeType.REMOVE], orphanRemoval = true, mappedBy = "recipient")
  var userNotifications: MutableList<UserNotification> = mutableListOf()

  @Where(clause = "project_id IS NOT NULL")
  @OneToMany(fetch = FetchType.LAZY, cascade = [CascadeType.REMOVE], orphanRemoval = true, mappedBy = "userAccount")
  var projectNotificationPreferences: MutableList<NotificationPreferences> = mutableListOf()

  @Where(clause = "project_id IS NULL")
  @OneToMany(fetch = FetchType.LAZY, cascade = [CascadeType.REMOVE], orphanRemoval = true, mappedBy = "userAccount")
  private var _globalNotificationPreferences: MutableList<NotificationPreferences> = mutableListOf()

  val globalNotificationPreferences: NotificationPreferences?
    get() = _globalNotificationPreferences.firstOrNull()

  enum class Role {
    USER,
    ADMIN,
  }

  enum class AccountType {
    LOCAL,
    MANAGED,
    THIRD_PARTY,
  }
}
