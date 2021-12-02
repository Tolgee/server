package io.tolgee.repository.machineTranslation

import io.tolgee.model.MtServiceConfig
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface MtServiceConfigRepository : JpaRepository<MtServiceConfig, Long> {
  @Query("""
    select ptsc from MtServiceConfig ptsc
    join Language l on l.id = :languageId 
        and (
            ptsc.targetLanguage.id = :languageId 
            or (ptsc.targetLanguage.id is null and ptsc.project.id = l.id)
            )
  """)
  fun findAllByTargetLanguageId(languageId: Long): List<MtServiceConfig>

  fun findAllByProjectId(projectId: Long): List<MtServiceConfig>
}
