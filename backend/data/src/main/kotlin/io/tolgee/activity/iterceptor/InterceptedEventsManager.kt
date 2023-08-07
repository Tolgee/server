package io.tolgee.activity.iterceptor

import io.tolgee.activity.ActivityHolder
import io.tolgee.activity.EntityDescriptionProvider
import io.tolgee.activity.annotation.ActivityLoggedEntity
import io.tolgee.activity.annotation.ActivityLoggedProp
import io.tolgee.activity.data.EntityDescriptionRef
import io.tolgee.activity.data.EntityDescriptionWithRelations
import io.tolgee.activity.data.PropertyModification
import io.tolgee.activity.data.RevisionType
import io.tolgee.activity.propChangesProvider.PropChangesProvider
import io.tolgee.dtos.cacheable.UserAccountDto
import io.tolgee.model.EntityWithId
import io.tolgee.model.activity.ActivityDescribingEntity
import io.tolgee.model.activity.ActivityModifiedEntity
import io.tolgee.model.activity.ActivityRevision
import io.tolgee.security.AuthenticationFacade
import io.tolgee.security.project_auth.ProjectHolder
import io.tolgee.security.project_auth.ProjectNotSelectedException
import org.hibernate.Transaction
import org.hibernate.collection.internal.AbstractPersistentCollection
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.config.BeanDefinition.SCOPE_SINGLETON
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import java.io.Serializable
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.hasAnnotation

@Component
@Scope(SCOPE_SINGLETON)
class InterceptedEventsManager(
  private val applicationContext: ApplicationContext
) {
  private val logger = LoggerFactory.getLogger(this::class.java)

  fun onAfterTransactionCompleted(tx: Transaction) {
    activityHolder.transactionRollbackOnly = tx.rollbackOnly
  }

  fun onCollectionModification(collection: Any?, key: Serializable?) {
    if (collection !is AbstractPersistentCollection || collection !is Collection<*> || key !is Long) {
      return
    }

    val collectionOwner = collection.owner
    if (collectionOwner !is EntityWithId ||
      !collectionOwner::class.hasAnnotation<ActivityLoggedEntity>()
    ) {
      return
    }

    val ownerField = collectionOwner::class.members.find {
      it.parameters.size == 1 && it.call(collectionOwner) === collection
    } ?: return

    val provider = getChangesProvider(collectionOwner, ownerField.name) ?: return

    val stored = (collection.storedSnapshot as? HashMap<*, *>)?.values?.toList()

    val old = activityHolder.modifiedCollections.computeIfAbsent(collectionOwner to ownerField.name) {
      stored
    }

    val changes = provider.getChanges(old, collection) ?: return
    val activityModifiedEntity = getModifiedEntity(collectionOwner)

    val newChanges = activityModifiedEntity.modifications + mutableMapOf(ownerField.name to changes)
    activityModifiedEntity.modifications = (newChanges).toMutableMap()

    activityModifiedEntity.setEntityDescription(collectionOwner)
  }

  fun onFieldModificationsActivity(
    entity: Any?,
    currentState: Array<out Any>?,
    previousState: Array<out Any>?,
    propertyNames: Array<out String>?,
    revisionType: RevisionType
  ) {
    if (!shouldHandleActivity(entity)) {
      return
    }

    entity as EntityWithId

    val activityModifiedEntity = getModifiedEntity(entity)

    val changesMap = getChangesMap(entity, currentState, previousState, propertyNames)

    activityModifiedEntity.revisionType = revisionType
    activityModifiedEntity.modifications.putAll(changesMap)

    activityModifiedEntity.setEntityDescription(entity)
  }

  private fun ActivityModifiedEntity.setEntityDescription(
    entity: EntityWithId,
  ) {
    val describingData = getChangeEntityDescription(entity, activityRevision)
    this.describingData = describingData.first?.filter { !this.modifications.keys.contains(it.key) }
    this.describingRelations = describingData.second
  }

  private fun getModifiedEntity(entity: EntityWithId): ActivityModifiedEntity {
    val activityModifiedEntity = activityHolder.modifiedEntities
      .computeIfAbsent(entity::class) { mutableMapOf() }
      .computeIfAbsent(
        entity.id
      ) {
        ActivityModifiedEntity(
          activityRevision,
          entity::class.simpleName!!,
          entity.id
        )
      }

    return activityModifiedEntity
  }

  private fun getChangeEntityDescription(
    entity: EntityWithId,
    activityRevision: ActivityRevision
  ): Pair<Map<String, Any?>?, Map<String, EntityDescriptionRef>?> {
    val rootDescription = applicationContext.getBean(EntityDescriptionProvider::class.java).getDescriptionWithRelations(
      entity
    )
    val relations = rootDescription?.relations
      ?.map { it.key to compressRelation(it.value, activityRevision) }
      ?.toMap()

    return (rootDescription?.data to relations)
  }

  private fun compressRelation(
    value: EntityDescriptionWithRelations,
    activityRevision: ActivityRevision
  ): EntityDescriptionRef {
    val activityDescribingEntity = activityRevision.describingRelations
      .find { it.entityId == value.entityId && it.entityClass == value.entityClass }
      ?: let {
        val compressedRelations = value.relations.map { relation ->
          relation.key to compressRelation(relation.value, activityRevision)
        }.toMap()

        val activityDescribingEntity = ActivityDescribingEntity(
          activityRevision,
          value.entityClass,
          value.entityId
        )

        activityDescribingEntity.data = value.data
        activityDescribingEntity.describingRelations = compressedRelations
        activityRevision.describingRelations.add(activityDescribingEntity)

        activityDescribingEntity
      }
    return EntityDescriptionRef(activityDescribingEntity.entityClass, activityDescribingEntity.entityId)
  }

  private fun getChangesMap(
    entity: Any,
    currentState: Array<out Any>?,
    previousState: Array<out Any>?,
    propertyNames: Array<out String>?
  ): Map<String, PropertyModification> {
    if (propertyNames == null) {
      return mapOf()
    }

    return propertyNames.asSequence().mapIndexedNotNull { idx, propertyName ->
      val old = previousState?.get(idx)
      val new = currentState?.get(idx)
      val provider = getChangesProvider(entity, propertyName) ?: return@mapIndexedNotNull null
      propertyName to (provider.getChanges(old, new) ?: return@mapIndexedNotNull null)
    }.toMap()
  }

  fun getChangesProvider(entity: Any, propertyName: String): PropChangesProvider? {
    val propertyAnnotation = entity::class.members
      .find { it.name == propertyName }
      ?.findAnnotation<ActivityLoggedProp>()
      ?: return null

    val providerClass = propertyAnnotation.modificationProvider
    return applicationContext.getBean(providerClass.java)
  }

  private fun shouldHandleActivity(entity: Any?) =
    entity is EntityWithId && entity::class.hasAnnotation<ActivityLoggedEntity>() &&
      !entity.disableActivityLogging

  private val activityRevision: ActivityRevision
    get() {
      if (!activityHolder.activityRevision.isInitializedByInterceptor) {
        activityHolder.activityRevision.isInitializedByInterceptor = true
        activityHolder.activityRevision.also { revision ->
          revision.authorId = userAccount?.id
          try {
            revision.projectId = projectHolder.project.id
            activityHolder.organizationId = projectHolder.project.organizationOwnerId
          } catch (e: ProjectNotSelectedException) {
            logger.info("Project is not set in ProjectHolder. Activity will be stored without projectId.")
          }
          revision.type = activityHolder.activity
        }
      }

      return activityHolder.activityRevision
    }

  private val userAccount: UserAccountDto?
    get() = applicationContext.getBean(AuthenticationFacade::class.java).userAccountOrNull

  private val activityHolder: ActivityHolder
    get() = applicationContext.getBean(ActivityHolder::class.java)

  private val projectHolder: ProjectHolder
    get() = applicationContext.getBean(ProjectHolder::class.java)
}
