package io.tolgee.development.testDataBuilder.data

import io.tolgee.development.testDataBuilder.builders.ProjectBuilder
import io.tolgee.development.testDataBuilder.builders.TestDataBuilder
import io.tolgee.development.testDataBuilder.builders.UserAccountBuilder
import io.tolgee.model.Organization
import io.tolgee.model.UserAccount
import io.tolgee.model.automations.*
import io.tolgee.model.enums.ProjectPermissionType
import io.tolgee.model.enums.Scope
import io.tolgee.model.key.Key
import io.tolgee.model.slackIntegration.EventName
import io.tolgee.model.slackIntegration.OrganizationSlackWorkspace
import io.tolgee.model.slackIntegration.SlackConfig
import io.tolgee.model.slackIntegration.SlackUserConnection

class SlackTestData() {
  var user: UserAccount
  var slackConfig: SlackConfig
  var userAccountBuilder: UserAccountBuilder
  var projectBuilder: ProjectBuilder
  var organization: Organization
  var automation: Automation
  var key: Key
  var key2: Key
  lateinit var slackWorkspace: OrganizationSlackWorkspace
  lateinit var slackUserConnection: SlackUserConnection

  val root: TestDataBuilder =
    TestDataBuilder().apply {
      userAccountBuilder =
        addUserAccount {
          username = "admin"
        }

      userAccountBuilder.addSlackUserConnection {
        userAccount = userAccountBuilder.self
        slackUserId = "slackUserId"
        slackUserConnection = this
      }

      projectBuilder =
        addProject {
          name = "projectName"
          organizationOwner = userAccountBuilder.defaultOrganizationBuilder.self
        }.build buildProject@{
          this@buildProject.self.baseLanguage = this@buildProject.addEnglish().self
        }
      projectBuilder.addKey("testKey").also { key = it.self }
        .addTranslation("en", "Hello")

      projectBuilder.addKey("testKey2").also { key2 = it.self }
        .addTranslation("en", "Hello")

      userAccountBuilder.defaultOrganizationBuilder.addSlackWorkspace {
        author = userAccountBuilder.self
        slackTeamId = "slackTeamId"
        slackTeamName = "slackTeamName"
        accessToken = "accessToken"
        organization = userAccountBuilder.defaultOrganizationBuilder.self
        slackWorkspace = this
      }
      organization = userAccountBuilder.defaultOrganizationBuilder.self

      user = userAccountBuilder.self

      projectBuilder.addPermission {
        project = projectBuilder.self
        user = user
        type = ProjectPermissionType.MANAGE
        scopes = arrayOf(Scope.TRANSLATIONS_EDIT)
      }

      projectBuilder.addFrench()

      projectBuilder.addCzech()

      slackConfig =
        projectBuilder.addSlackConfig {
          this.channelId = "testChannel"
          this.project = projectBuilder.self
          this.userAccount = userAccountBuilder.self
          isGlobalSubscription = true
          events = mutableSetOf(EventName.ALL)
        }.build config@{
          addSlackMessage {
            slackConfig = this@config.self
            this.keyId = 0L
            this.languageTags = mutableSetOf("en", "fr")
          }

          addSlackMessage {
            slackConfig = this@config.self
            this.keyId = 0L
            this.languageTags = mutableSetOf("fr", "cz")
          }

          addSlackMessage {
            slackConfig = this@config.self
            this.keyId = 1L
            this.languageTags = mutableSetOf("cz", "ru")
          }

          addSlackMessage {
            slackConfig = this@config.self
            this.keyId = 52L
            this.languageTags = mutableSetOf("fr", "cz")
          }
        }.self

      automation =
        projectBuilder.addAutomation {
          this.triggers.add(
            AutomationTrigger(this)
              .also { it.type = AutomationTriggerType.ACTIVITY },
          )
          this.actions.add(
            AutomationAction(this).also {
              it.type = AutomationActionType.SLACK_SUBSCRIPTION
              it.slackConfig = slackConfig
            },
          )
        }.self
    }
}
