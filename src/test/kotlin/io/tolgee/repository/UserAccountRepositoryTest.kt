package io.tolgee.repository

import io.tolgee.AbstractSpringTest
import io.tolgee.assertions.Assertions.assertThat
import io.tolgee.development.DbPopulatorReal
import io.tolgee.model.Permission
import io.tolgee.model.enums.OrganizationRoleType
import io.tolgee.model.views.UserAccountWithOrganizationRoleView
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.PageRequest
import org.testng.annotations.Test

@SpringBootTest
class UserAccountRepositoryTest : AbstractSpringTest() {

    @Autowired
    lateinit var userAccountRepository: UserAccountRepository

    @Autowired
    lateinit var dbPopulatorReal: DbPopulatorReal

    @Test
    fun getAllInOrganizationHasMemberRole() {
        val usersAndOrganizations = dbPopulatorReal.createUsersAndOrganizations()
        val org = usersAndOrganizations[1].organizationRoles[0].organization
        val returned = userAccountRepository.getAllInOrganization(org!!.id!!, PageRequest.of(0, 20), "")
        assertThat(returned.content).hasSize(2)
        assertThat(returned.content[0]).isInstanceOf(UserAccountWithOrganizationRoleView::class.java)
    }

    @Test
    fun getAllInOrganizationCorrectAccounts() {
        val usersAndOrganizations = dbPopulatorReal.createUsersAndOrganizations()
        val user = entityManager.merge(usersAndOrganizations[2])
        entityManager.refresh(user)

        val org = user.organizationRoles[0].organization
        val returned = userAccountRepository.getAllInOrganization(org!!.id!!, PageRequest.of(0, 20), "")
        assertThat(returned.content).hasSize(3)
    }


    @Test
    fun getAllInProject() {
        val franta = dbPopulatorReal.createUserIfNotExists("franta")
        val usersAndOrganizations = dbPopulatorReal.createUsersAndOrganizations()
        val repo = usersAndOrganizations[0].organizationRoles[0].organization!!.projects[0]

        permissionService.grantFullAccessToRepo(franta, repo)

        val returned = userAccountRepository.getAllInProject(repo.id, PageRequest.of(0, 20))
        assertThat(returned.content).hasSize(3)
        assertThat(returned.content[0].directPermissions).isEqualTo(Permission.ProjectPermissionType.MANAGE)
        assertThat(returned.content[1].organizationRole).isEqualTo(OrganizationRoleType.MEMBER)
        assertThat(returned.content[2].organizationRole).isEqualTo(OrganizationRoleType.OWNER)
    }

    @Test
    fun getAllInProjectSearch() {
        val franta = dbPopulatorReal.createUserIfNotExists("franta")
        val usersAndOrganizations = dbPopulatorReal.createUsersAndOrganizations()
        val repo = usersAndOrganizations[0].organizationRoles[0].organization!!.projects[0]

        permissionService.grantFullAccessToRepo(franta, repo)

        val returned = userAccountRepository.getAllInProject(repo.id, PageRequest.of(0, 20), "franta")
        assertThat(returned.content).hasSize(1)
    }
}
