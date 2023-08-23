/**
 * Copyright (C) 2023 Tolgee s.r.o. and contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.tolgee.security

import io.tolgee.model.ApiKey
import io.tolgee.model.Organization
import io.tolgee.model.Project
import io.tolgee.security.authentication.AuthenticationFacade
import io.tolgee.security.project_auth.ProjectNotSelectedException
import io.tolgee.service.organization.OrganizationService
import io.tolgee.service.project.ProjectService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.web.servlet.HandlerMapping

class RequestContextServiceTest {
  companion object {
    const val TEST_PROJECT_ID = 1337L
    const val TEST_ORGANIZATION_ID = 418L
    const val TEST_ORGANIZATION_SLUG = "teapot-org"
  }

  private val authenticationFacade = Mockito.mock(AuthenticationFacade::class.java)

  private val projectService = Mockito.mock(ProjectService::class.java)

  private val organizationService = Mockito.mock(OrganizationService::class.java)

  private val project = Mockito.mock(Project::class.java)

  private val organization = Mockito.mock(Organization::class.java)

  private val apiKey = Mockito.mock(ApiKey::class.java)

  private val requestContextService = RequestContextService(authenticationFacade, projectService, organizationService)

  @BeforeEach
  fun setupMocks() {
    Mockito.`when`(projectService.find(Mockito.anyLong())).thenReturn(null)
    Mockito.`when`(projectService.find(TEST_PROJECT_ID)).thenReturn(project)

    Mockito.`when`(organizationService.find(Mockito.anyLong())).thenReturn(null)
    Mockito.`when`(organizationService.find(TEST_ORGANIZATION_ID)).thenReturn(organization)

    Mockito.`when`(organizationService.find(Mockito.anyString())).thenReturn(null)
    Mockito.`when`(organizationService.find(TEST_ORGANIZATION_SLUG)).thenReturn(organization)

    Mockito.`when`(project.id).thenReturn(TEST_PROJECT_ID)
    Mockito.`when`(organization.id).thenReturn(TEST_ORGANIZATION_ID)
    Mockito.`when`(organization.slug).thenReturn(TEST_ORGANIZATION_SLUG)

    Mockito.`when`(apiKey.project).thenReturn(project)
  }

  @AfterEach
  fun resetMocks() {
    Mockito.reset(authenticationFacade, projectService, organizationService, project, organization)
  }

  private fun setupApiKey() {
    Mockito.`when`(authenticationFacade.isApiAuthentication).thenReturn(true)
    Mockito.`when`(authenticationFacade.isProjectApiKeyAuth).thenReturn(true)
    Mockito.`when`(authenticationFacade.projectApiKey).thenReturn(apiKey)
  }

  private fun makeRequest(path: String, id: String = ""): MockHttpServletRequest {
    val req = MockHttpServletRequest("GET", path.replace("{id}", id))
    req.setAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE, path)
    req.setAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, mapOf("id" to id))
    return req
  }

  @Test
  fun `it correctly detects the current project`() {
    val reqCommon = makeRequest("/v2/projects/{id}/keys", TEST_PROJECT_ID.toString())
    val reqOldApi = makeRequest("/api/project/{id}/keys", TEST_PROJECT_ID.toString())
    val reqOldRepo = makeRequest("/api/repository/{id}/keys", TEST_PROJECT_ID.toString())

    val project1 = requestContextService.getTargetProject(reqCommon)
    val project2 = requestContextService.getTargetProject(reqOldApi)
    val project3 = requestContextService.getTargetProject(reqOldRepo)

    assertThat(project1?.id).isEqualTo(TEST_PROJECT_ID)
    assertThat(project2?.id).isEqualTo(TEST_PROJECT_ID)
    assertThat(project3?.id).isEqualTo(TEST_PROJECT_ID)
  }

  @Test
  fun `it correctly detects the current project in implicit scenarios`() {
    setupApiKey()

    val reqCommon = makeRequest("/v2/projects/keys")
    val reqOldApi = makeRequest("/api/project/keys")
    val reqOldRepo = makeRequest("/api/repository/keys")
    val reqExplicit = makeRequest("/v2/projects/{id}/keys", (TEST_PROJECT_ID + 1).toString())

    val project1 = requestContextService.getTargetProject(reqCommon)
    val project2 = requestContextService.getTargetProject(reqOldApi)
    val project3 = requestContextService.getTargetProject(reqOldRepo)
    val project4 = requestContextService.getTargetProject(reqExplicit)

    assertThat(project1?.id).isEqualTo(TEST_PROJECT_ID)
    assertThat(project2?.id).isEqualTo(TEST_PROJECT_ID)
    assertThat(project3?.id).isEqualTo(TEST_PROJECT_ID)
    assertThat(project4).isNull() // Null because no project TEST_PROJECT_ID + 1 exists

    Mockito.verify(projectService, Mockito.times(1)).find(TEST_PROJECT_ID + 1)
  }

  @Test
  fun `it throws in implicit scenarios without an API key`() {
    val reqCommon = makeRequest("/v2/projects/keys")
    val reqOldApi = makeRequest("/api/project/keys")
    val reqOldRepo = makeRequest("/api/repository/keys")

    assertThrows<ProjectNotSelectedException> { requestContextService.getTargetProject(reqCommon) }
    assertThrows<ProjectNotSelectedException> { requestContextService.getTargetProject(reqOldApi) }
    assertThrows<ProjectNotSelectedException> { requestContextService.getTargetProject(reqOldRepo) }
  }

  @Test
  fun `it correctly detects the current organization`() {
    val req = makeRequest("/v2/organizations/{id}/projects", TEST_ORGANIZATION_ID.toString())
    val org = requestContextService.getTargetOrganization(req)

    assertThat(org?.id).isEqualTo(TEST_ORGANIZATION_ID)
    assertThat(org?.slug).isEqualTo(TEST_ORGANIZATION_SLUG)
  }

  @Test
  fun `it correctly detects the current organization when using slugs`() {
    val req = makeRequest("/v2/organizations/{id}/projects", TEST_ORGANIZATION_SLUG)
    val org = requestContextService.getTargetOrganization(req)

    assertThat(org?.id).isEqualTo(TEST_ORGANIZATION_ID)
    assertThat(org?.slug).isEqualTo(TEST_ORGANIZATION_SLUG)
  }
}
