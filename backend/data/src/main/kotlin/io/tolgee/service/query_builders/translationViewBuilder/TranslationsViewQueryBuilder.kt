package io.tolgee.service.query_builders.translationViewBuilder

import io.tolgee.dtos.request.translation.TranslationFilters
import io.tolgee.dtos.response.CursorValue
import io.tolgee.model.*
import org.springframework.data.domain.*
import java.util.*
import javax.persistence.criteria.*

class TranslationsViewQueryBuilder(
  private val cb: CriteriaBuilder,
  private val projectId: Long,
  private val languages: Set<Language>,
  private val params: TranslationFilters,
  private val sort: Sort,
  cursor: Map<String, CursorValue>? = null,
) {
  private lateinit var queryBase: QueryBase<*>

  private val cursorPredicateProvider = CursorPredicateProvider(cb, cursor, queryBase.querySelection)

  private fun <T> getBaseQuery(query: CriteriaQuery<T>): CriteriaQuery<T> {
    queryBase = QueryBase(cb, projectId, query, languages, params)
    return query
  }

  val dataQuery: CriteriaQuery<Array<Any?>>
    get() {
      val query = getBaseQuery(cb.createQuery(Array<Any?>::class.java))
      val paths = queryBase.querySelection.values.toTypedArray()
      query.multiselect(*paths)
      val orderList = sort.asSequence().filter { queryBase.querySelection[it.property] != null }.map {
        val expression = queryBase.querySelection[it.property] as Expression<*>
        when (it.direction) {
          Sort.Direction.DESC -> cb.desc(expression)
          else -> cb.asc(expression)
        }
      }.toMutableList()

      if (orderList.isEmpty()) {
        orderList.add(cb.asc(queryBase.keyNameExpression))
      }
      val where = queryBase.whereConditions.toMutableList()
      cursorPredicateProvider()?.let {
        where.add(it)
      }
      val groupBy = listOf(queryBase.keyIdExpression, *queryBase.groupByExpressions.toTypedArray())
      query.where(*where.toTypedArray())
      query.groupBy(groupBy)
      query.orderBy(orderList)
      return query
    }

  val countQuery: CriteriaQuery<Long>
    get() {
      val query = getBaseQuery(cb.createQuery(Long::class.java))
      val file = query.roots.iterator().next() as Root<*>
      query.select(cb.countDistinct(file))
      query.where(*queryBase.whereConditions.toTypedArray())
      return query
    }

  val keyIdsQuery: CriteriaQuery<Long>
    get() {
      queryBase.isKeyIdsQuery = true
      val query = getBaseQuery(cb.createQuery(Long::class.java))
      query.select(queryBase.keyIdExpression)
      query.where(*queryBase.whereConditions.toTypedArray())
      query.distinct(true)
      return query
    }
}
