package io.tolgee.repository.dataImport

import io.tolgee.model.dataImport.Import
import io.tolgee.model.dataImport.ImportLanguage
import io.tolgee.model.dataImport.ImportTranslation
import io.tolgee.model.views.ImportTranslationView
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
interface ImportTranslationRepository : JpaRepository<ImportTranslation, Long> {
  @Query(
    """
        select distinct it from ImportTranslation it
        join fetch it.key ik
        left join fetch ik.keyMeta
        left join fetch it.conflict ic
        left join fetch ic.key ick
        left join fetch ick.keyMeta
        join it.language il on il.id = :languageId
        join il.file if
        """,
  )
  fun findAllByImportAndLanguageId(languageId: Long): List<ImportTranslation>

  @Modifying
  @Transactional
  @Query("update ImportTranslation it set it.conflict = null where it.conflict.id in :translationIds")
  fun removeExistingTranslationConflictReferences(translationIds: Collection<Long>)

  @Query(
    """ select it.id as id, it.text as text, ik.name as keyName, ik.id as keyId,
        itc.id as conflictId, itc.text as conflictText, it.override as override, it.resolvedHash as resolvedHash
        from ImportTranslation it left join it.conflict itc join it.key ik
        where (itc.id is not null or :onlyConflicts = false)
        and ((itc.id is not null and it.resolvedHash is null) or :onlyUnresolved = false)
        and it.language.id = :languageId
        and (:search is null or lower(it.text) like lower(concat('%', cast(:search as text), '%'))
        or lower(ik.name) like lower(concat('%', cast(:search as text), '%')))
    """,
  )
  fun findImportTranslationsView(
    languageId: Long,
    pageable: Pageable,
    onlyConflicts: Boolean = false,
    onlyUnresolved: Boolean = false,
    search: String? = null,
  ): Page<ImportTranslationView>

  @Transactional
  @Query("delete from ImportTranslation it where it.language = :language")
  @Modifying
  fun deleteAllByLanguage(language: ImportLanguage)

  @Transactional
  @Query(
    """delete from ImportTranslation it where it.key.id in 
        (select k.id from ImportKey k join k.file f where f.import = :import)""",
  )
  @Modifying
  fun deleteAllByImport(import: Import)
}
