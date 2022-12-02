package io.tolgee.security

import io.tolgee.fixtures.andIsForbidden
import io.tolgee.fixtures.andIsOk
import io.tolgee.fixtures.generateUniqueString
import io.tolgee.security.project_auth.ProjectHolder
import io.tolgee.testing.AuthorizedControllerTest
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional

@Transactional
class ProjectPermissionFilterTest : AuthorizedControllerTest() {

  @field:Autowired
  lateinit var projectHolder: ProjectHolder

  @Test
  fun allowsAccessToPrivilegedUser() {
    val base = dbPopulator.createBase(generateUniqueString())
    performAuthGet("/v2/projects/${base.project.id}/translations").andIsOk
  }

  @Test
  fun deniesAccessToNonPrivilegedUser() {
    val base2 = dbPopulator.createBase(generateUniqueString(), "new-user")
    performAuthGet("/v2/projects/${base2.project.id}/translations").andIsForbidden
  }
}
