package io.tolgee.service.query_builders.translationViewBuilder

import io.tolgee.dtos.request.translation.TranslationFilters
import io.tolgee.model.Language
import io.tolgee.model.Project_
import io.tolgee.model.Screenshot
import io.tolgee.model.Screenshot_
import io.tolgee.model.enums.TranslationCommentState
import io.tolgee.model.enums.TranslationState
import io.tolgee.model.key.Key
import io.tolgee.model.key.Key_
import io.tolgee.model.key.Namespace_
import io.tolgee.model.translation.Translation
import io.tolgee.model.translation.TranslationComment_
import io.tolgee.model.translation.Translation_
import io.tolgee.model.views.KeyWithTranslationsView
import io.tolgee.model.views.TranslationView
import javax.persistence.criteria.CriteriaBuilder
import javax.persistence.criteria.CriteriaQuery
import javax.persistence.criteria.Expression
import javax.persistence.criteria.Join
import javax.persistence.criteria.JoinType
import javax.persistence.criteria.Path
import javax.persistence.criteria.Predicate
import javax.persistence.criteria.Root
import javax.persistence.criteria.SetJoin

class QueryBase<T>(
  private val cb: CriteriaBuilder,
  private val projectId: Long,
  private val query: CriteriaQuery<T>,
  private val languages: Set<Language>,
  params: TranslationFilters
) {
  val whereConditions: MutableSet<Predicate> = HashSet()
  val root: Root<Key> = query.from(Key::class.java)
  val keyNameExpression: Path<String> = root.get(Key_.name)
  val keyIdExpression: Path<Long> = root.get(Key_.id)
  val querySelection = QuerySelection()
  val fullTextFields: MutableSet<Expression<String>> = HashSet()
  lateinit var namespaceNameExpression: Path<String>
  var translationsTextFields: MutableSet<Expression<String>> = HashSet()
  lateinit var screenshotCountExpression: Expression<Long>
  val groupByExpressions: MutableSet<Expression<*>> = mutableSetOf()
  private val queryGlobalFiltering = QueryGlobalFiltering(params, this, cb)
  var queryTranslationFiltering = QueryTranslationFiltering(params, this, cb)
  var isKeyIdsQuery = false

  init {
    querySelection[KeyWithTranslationsView::keyId.name] = keyIdExpression
    querySelection[KeyWithTranslationsView::keyName.name] = keyNameExpression
    whereConditions.add(cb.equal(root.get<Any>(Key_.PROJECT).get<Any>(Project_.ID), this.projectId))
    fullTextFields.add(root.get(Key_.name))
    addLeftJoinedColumns()
    queryGlobalFiltering.apply()
  }

  private fun addLeftJoinedColumns() {
    addNamespace()
    addScreenshotCounts()
    addLanguageSpecificFields()
  }

  private fun addLanguageSpecificFields() {
    for (language in languages) {
      val translation = addTranslationId(language)
      val translationTextField = addTranslationText(translation, language)
      this.fullTextFields.add(translationTextField)
      translationsTextFields.add(translationTextField)

      val translationStateField = addTranslationStateField(translation, language)
      queryTranslationFiltering.apply(language, translationTextField, translationStateField)

      addNotFilteringTranslationFields(language, translation)
      addComments(translation, language)
    }
  }

  private fun addComments(
    translation: SetJoin<Key, Translation>,
    language: Language
  ) {
    val commentsJoin = translation.join(Translation_.comments, JoinType.LEFT)
    val commentsExpression = cb.countDistinct(commentsJoin)
    this.querySelection[language to TranslationView::commentCount] = commentsExpression

    val unresolvedCommentsJoin = translation.join(Translation_.comments, JoinType.LEFT)
    unresolvedCommentsJoin.on(
      cb.and(
        cb.equal(
          unresolvedCommentsJoin.get(TranslationComment_.translation),
          translation
        ),
        cb.equal(unresolvedCommentsJoin.get(TranslationComment_.state), TranslationCommentState.NEEDS_RESOLUTION)
      )
    )

    val unresolvedCommentsExpression = cb.countDistinct(unresolvedCommentsJoin)
    this.querySelection[language to TranslationView::unresolvedCommentCount] = unresolvedCommentsExpression
  }

  private fun addTranslationStateField(
    translation: SetJoin<Key, Translation>,
    language: Language
  ): Path<TranslationState> {
    val translationStateField = translation.get(Translation_.state)
    this.querySelection[language to TranslationView::state] = translationStateField
    return translationStateField
  }

  private fun addTranslationText(
    translation: SetJoin<Key, Translation>,
    language: Language
  ): Path<String> {
    val translationTextField = translation.get(Translation_.text)
    this.querySelection[language to TranslationView::text] = translationTextField
    return translationTextField
  }

  private fun addTranslationId(language: Language): SetJoin<Key, Translation> {
    val translation = this.root.join(Key_.translations, JoinType.LEFT)
    translation.on(cb.equal(translation.get(Translation_.language), language))
    val translationId = translation.get(Translation_.id)
    this.querySelection[language to TranslationView::id] = translationId
    groupByExpressions.add(translationId)
    return translation
  }

  private fun addScreenshotCounts() {
    val screenshotSubquery = this.query.subquery(Long::class.java)
    val screenshotRoot = screenshotSubquery.from(Screenshot::class.java)
    val screenshotCount = cb.count(screenshotRoot.get(Screenshot_.id))
    screenshotSubquery.select(screenshotCount)
    val screenshotsJoin: Join<Screenshot, Key> = screenshotRoot.join(Screenshot_.key)
    screenshotSubquery.where(cb.equal(this.root.get(Key_.id), screenshotsJoin.get(Key_.id)))
    screenshotCountExpression = screenshotSubquery.selection
    this.querySelection[KeyWithTranslationsView::screenshotCount.name] = screenshotCountExpression
  }

  private fun addNotFilteringTranslationFields(
    language: Language,
    translation: SetJoin<Key, Translation>
  ) {
    if (!isKeyIdsQuery) {
      this.querySelection[language to TranslationView::auto] = translation.get(Translation_.auto)
      this.querySelection[language to TranslationView::mtProvider] = translation.get(Translation_.mtProvider)
    }
  }

  private fun addNamespace() {
    val namespace = this.root.join(Key_.namespace, JoinType.LEFT)
    val namespaceName = namespace.get(Namespace_.name)
    namespaceNameExpression = namespaceName
    this.querySelection[KeyWithTranslationsView::keyNamespace.name] = namespaceName
    this.fullTextFields.add(namespaceName)
    groupByExpressions.add(namespaceName)
  }

  val Expression<String>.isNotNullOrBlank: Predicate
    get() = cb.and(cb.isNotNull(this), cb.notEqual(this, ""))

  val Expression<String>.isNullOrBlank: Predicate
    get() = cb.or(cb.isNull(this), cb.equal(this, ""))
}
