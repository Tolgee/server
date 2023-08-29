package io.tolgee.util

import javax.persistence.EntityManager
import javax.persistence.TypedQuery
import javax.persistence.criteria.CriteriaBuilder
import javax.persistence.criteria.CriteriaQuery
import javax.persistence.criteria.Expression
import javax.persistence.criteria.Predicate
import javax.persistence.criteria.Root

fun CriteriaBuilder.greaterThanNullable(
  expression: Expression<String>,
  value: String?
): Predicate {
  if (value == null) {
    return expression.isNotNull
  }
  return this.and(expression.isNotNull, this.greaterThan(expression, value))
}

fun CriteriaBuilder.lessThanNullable(
  expression: Expression<String>,
  value: String?
): Predicate {
  if (value == null) {
    return this.isTrue(this.literal(false))
  }
  return this.or(expression.isNull, this.lessThan(expression, value))
}

fun CriteriaBuilder.greaterThanOrEqualToNullable(
  expression: Expression<String>,
  value: String?
): Predicate {
  if (value == null) {
    return this.isTrue(this.literal(true))
  }
  return this.and(expression.isNotNull, this.greaterThanOrEqualTo(expression, value))
}

fun CriteriaBuilder.lessThanOrEqualToNullable(
  expression: Expression<String>,
  value: String?
): Predicate {
  if (value == null) {
    return this.isNull(expression)
  }
  return this.or(expression.isNull, this.lessThanOrEqualTo(expression, value))
}

fun CriteriaBuilder.equalNullable(
  expression: Expression<String>,
  value: Any?
): Predicate {
  if (value == null) {
    return this.isNull(expression)
  }
  return this.equal(expression, value)
}

inline fun <reified RootT, reified Result> EntityManager.query(
  fn: CriteriaQuery<Result>.(cb: CriteriaBuilder, root: Root<RootT>) -> Unit
): TypedQuery<Result> {
  val cb = this.criteriaBuilder
  val cq = cb.createQuery(Result::class.java)
  val root = cq.from(RootT::class.java)
  fn(cq, cb, root)
  return this.createQuery(cq)
}
