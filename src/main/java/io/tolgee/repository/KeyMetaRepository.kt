package io.tolgee.repository

import io.tolgee.model.key.KeyMeta
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
interface KeyMetaRepository : JpaRepository<KeyMeta?, Long?> {

    @Modifying
    @Transactional
    @Query("""delete from KeyComment kc where kc.keyMeta in 
        (select km from kc.keyMeta km join km.key k where k.repository.id = :repositoryId)""")
    fun deleteAllKeyCommentsByRepositoryId(repositoryId: Long)


    @Modifying
    @Transactional
    @Query("""delete from KeyCodeReference kcr where kcr.keyMeta in 
        (select km from kcr.keyMeta km join km.key k where k.repository.id = :repositoryId)""")
    fun deleteAllKeyCodeReferencesByRepositoryId(repositoryId: Long)


    @Modifying
    @Transactional
    @Query("delete from KeyMeta km where km.key in (select k from km.key k where k.repository.id = :repositoryId)")
    fun deleteAllByRepositoryId(repositoryId: Long)

    @Modifying
    @Transactional
    @Query("delete from KeyMeta km where km.importKey.id in :keyIds")
    fun deleteAllByImportKeyIdIn(keyIds: List<Long>)
}
