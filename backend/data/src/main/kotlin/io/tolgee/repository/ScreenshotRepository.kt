package io.tolgee.repository

import io.tolgee.model.Screenshot
import io.tolgee.model.key.Key
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface ScreenshotRepository : JpaRepository<Screenshot, Long> {
  @Query("FROM Screenshot s join s.keyScreenshotReferences ksr where ksr.key = :key")
  fun findAllByKey(key: Key): List<Screenshot>

  @Query("FROM Screenshot s join s.keyScreenshotReferences ksr join ksr.key k where k.project.id = :projectId")
  fun getAllByKeyProjectId(projectId: Long): List<Screenshot>

  @Query("FROM Screenshot s join s.keyScreenshotReferences ksr where ksr.key.id = :id")
  fun getAllByKeyId(id: Long): List<Screenshot>

  @Query("FROM Screenshot s join s.keyScreenshotReferences ksr where ksr.key.id in :keyIds")
  fun getAllByKeyIdIn(keyIds: Collection<Long>): List<Screenshot>

  @Query("SELECT count(s.id) FROM Screenshot s join s.keyScreenshotReferences ksr where ksr.key = :key")
  fun countByKey(key: Key): Long

  @Query(
    """
    from Key k 
      join fetch k.keyScreenshotReferences ksr
      join fetch ksr.screenshot s
    where k.id in :keyIds
  """
  )
  fun getKeysWithScreenshots(keyIds: Collection<Long>): List<Key>
}
