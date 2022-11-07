package io.tolgee.controllers.internal.e2e_data

import io.swagger.v3.oas.annotations.Hidden
import io.tolgee.development.testDataBuilder.TestDataService
import io.tolgee.development.testDataBuilder.data.dataImport.ImportTestData
import io.tolgee.model.Permission
import io.tolgee.model.dataImport.Import
import io.tolgee.security.InternalController
import io.tolgee.service.dataImport.ImportService
import io.tolgee.service.project.ProjectService
import io.tolgee.service.security.UserAccountService
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.persistence.EntityManager

@RestController
@CrossOrigin(origins = ["*"])
@Hidden
@RequestMapping(value = ["internal/e2e-data/import"])
@Transactional
@InternalController
class ImportE2eDataController(
  private val importService: ImportService,
  private val entityManager: EntityManager,
  private val testDataService: TestDataService,
  private val projectService: ProjectService,
  private val userAccountService: UserAccountService
) {
  @GetMapping(value = ["/generate"])
  @Transactional
  fun generateBasicTestData(): Map<String, Any> {
    val data = ImportTestData()
    data.addFileIssues()
    testDataService.saveTestData(data.root)
    return mapOf<String, Any>("project" to mapOf<String, Any>("id" to data.project.id))
  }

  @GetMapping(value = ["/generate-with-long-text"])
  @Transactional
  fun generateWithLongText(): Map<String, Any> {
    val data = ImportTestData()
    data.importBuilder.data.importFiles[0].data.importTranslations[0].self {
      text = "Hello, I am translation, with pretty long long long long long long long long long " +
        "long long long long long long long long long long long long long long long long " +
        "long long long long long long long long long long text"
      conflict!!.text = "Hello, I am old translation, with pretty long long long long long long long long long " +
        "long long long long long long long long long long long long long long long long " +
        "long long long long long long long long long long text"
    }
    data.addFileIssues()
    testDataService.saveTestData(data.root)
    return mapOf<String, Any>("project" to mapOf<String, Any>("id" to data.project.id))
  }

  @GetMapping(value = ["/generate-applicable"])
  @Transactional
  fun generateApplicableTestData(): Map<String, Any> {
    val data = ImportTestData()
    data.setAllResolved()
    data.addFileIssues()
    data.importFrench.existingLanguage = data.french
    testDataService.saveTestData(data.root)
    return mapOf<String, Any>("project" to mapOf<String, Any>("id" to data.project.id))
  }

  @GetMapping(value = ["/generate-all-selected"])
  @Transactional
  fun generateAllSelectedTestData(): Map<String, Any> {
    val data = ImportTestData()
    data.addFileIssues()
    data.importFrench.existingLanguage = data.french
    testDataService.saveTestData(data.root)
    return mapOf<String, Any>("project" to mapOf<String, Any>("id" to data.project.id))
  }

  @GetMapping(value = ["/generate-lot-of-data"])
  @Transactional
  fun generateLotTestData(): Map<String, Any> {
    val data = ImportTestData()
    data.addFileIssues()
    data.addManyFileIssues()
    data.addManyTranslations()
    testDataService.saveTestData(data.root)
    return mapOf<String, Any>("project" to mapOf<String, Any>("id" to data.project.id))
  }

  @GetMapping(value = ["/generate-many-languages"])
  @Transactional
  fun addManyLanguages(): Map<String, Any> {
    val data = ImportTestData()
    val file = data.importBuilder.data.importFiles[0]
    (0..90).forEach {
      file.addImportLanguage {

        name = "lng $it"
      }
    }
    testDataService.saveTestData(data.root)
    return mapOf<String, Any>("project" to mapOf<String, Any>("id" to data.project.id))
  }

  @GetMapping(value = ["/generate-base"])
  @Transactional
  fun generateBaseData(): Map<String, Any> {
    val data = testDataService.saveTestData {
      val userAccountBuilder = addUserAccount {
        username = "franta"
        name = "Frantisek Dobrota"
      }

      userAccountBuilder.build buildUserAccount@{
        addProject {
          organizationOwner = userAccountBuilder.defaultOrganizationBuilder.self
          name = "Repo"
        }.build buildProject@{
          val english = addEnglish()
          addGerman()
          self.baseLanguage = english.self
          addPermission {
            type = Permission.ProjectPermissionType.MANAGE
            user = this@buildUserAccount.self
            project = this@buildProject.self
          }
        }
      }
    }
    return mapOf<String, Any>("id" to data.data.projects[0].self.id)
  }

  @GetMapping(value = ["/clean"])
  @Transactional
  fun cleanup() {
    entityManager.createQuery("select i from Import i").resultList.forEach {
      importService.deleteImport(it as Import)
    }
    userAccountService.find("franta")?.let {
      projectService.findAllPermitted(it).forEach { repo ->
        projectService.deleteProject(repo.id!!)
      }
      userAccountService.delete(it)
    }
  }
}
