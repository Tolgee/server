package io.tolgee.component

import org.hibernate.Session
import org.hibernate.internal.SessionImpl
import org.hibernate.resource.transaction.backend.jdbc.internal.JdbcResourceLocalTransactionCoordinatorImpl
import org.springframework.stereotype.Component
import java.sql.Savepoint
import java.util.*
import javax.persistence.EntityManager

@Component
class SavePointManager(
  private val entityManager: EntityManager
) {
  fun setSavepoint(): Savepoint? {
    var savepoint: Savepoint? = null
    getSession().doWork { connection -> savepoint = connection.setSavepoint(UUID.randomUUID().toString()) }
    return savepoint
  }

  fun rollbackSavepoint(savepoint: Savepoint?) {
    getSession().doWork { connection ->
      connection.rollback(savepoint)
    }

    val session = (getSession().session as? SessionImpl) ?: throw IllegalStateException("Session is not SessionImpl")
    val coordinatorGetter = session::class.java.getMethod("getTransactionCoordinator")
    coordinatorGetter.isAccessible = true
    val coordinator = coordinatorGetter.invoke(session) as? JdbcResourceLocalTransactionCoordinatorImpl
      ?: throw IllegalStateException("Transaction coordinator is not JdbcResourceLocalTransactionCoordinatorImpl")
    val delegateField = coordinator::class.java.getDeclaredField("physicalTransactionDelegate")
    delegateField.isAccessible = true
    val delegate =
      delegateField.get(coordinator) as? JdbcResourceLocalTransactionCoordinatorImpl.TransactionDriverControlImpl
        ?: throw IllegalStateException("Transaction delegate is not TransactionDriverControlImpl")
    delegateField.isAccessible = false
    val field = delegate::class.java.getDeclaredField("rollbackOnly")
    field.isAccessible = true
    field.set(delegate, false)
    field.isAccessible = false
  }

  fun getSession(): Session {
    return entityManager.unwrap(Session::class.java)
      ?: throw IllegalStateException("Session is null")
  }
}
