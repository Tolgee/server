/**
 * Copyright (C) 2023 Tolgee s.r.o. and contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.tolgee.model.notifications

import io.tolgee.model.Project
import io.tolgee.model.UserAccount
import io.tolgee.model.activity.ActivityModifiedEntity
import io.tolgee.model.batch.BatchJob
import io.tolgee.notifications.NotificationType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.JoinTable
import jakarta.persistence.ManyToMany
import jakarta.persistence.ManyToOne
import jakarta.persistence.OrderBy
import jakarta.persistence.SequenceGenerator
import jakarta.persistence.Temporal
import jakarta.persistence.TemporalType
import org.hibernate.annotations.ColumnDefault
import org.hibernate.annotations.UpdateTimestamp
import java.util.*

@Entity
class UserNotification(
  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  val type: NotificationType,
  // This data is very likely to be useless when consuming the entity: lazy
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(nullable = false)
  val recipient: UserAccount,
  // We most definitely need this to show the notification: eager
  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(nullable = true)
  val project: Project?,
  // We most definitely need this to show the notification: eager
  @ManyToMany(fetch = FetchType.EAGER)
  @JoinTable(name = "user_notification_modified_entities")
  val modifiedEntities: MutableList<ActivityModifiedEntity> = mutableListOf(),
  // We most definitely need this to show the notification: eager
  @ManyToOne(fetch = FetchType.EAGER)
  val batchJob: BatchJob? = null,
) {
  @Id
  @SequenceGenerator(name = "notification_seq", sequenceName = "sequence_notifications")
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "notification_seq")
  val id: Long = 0

  @Column(nullable = false)
  @ColumnDefault("true")
  var unread: Boolean = true

  @Temporal(TemporalType.TIMESTAMP)
  var markedDoneAt: Date? = null

  @OrderBy
  @UpdateTimestamp
  @Temporal(TemporalType.TIMESTAMP)
  val lastUpdated: Date = Date()
}
