package io.tolgee.activity.iterceptor

import io.tolgee.events.OnEntityPreDelete
import io.tolgee.events.OnEntityPrePersist
import io.tolgee.events.OnEntityPreUpdate
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Component

@Component
class PreCommitEventPublisher(private val applicationContext: ApplicationContext) {
  fun onPersist(entity: Any?) {
    applicationContext.publishEvent(OnEntityPrePersist(this, entity))
  }

  fun onUpdate(entity: Any?) {
    applicationContext.publishEvent(OnEntityPreUpdate(this, entity))
  }

  fun onDelete(entity: Any?) {
    applicationContext.publishEvent(OnEntityPreDelete(this, entity))
  }
}
