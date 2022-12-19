package io.tolgee.constants

interface Caches {
  companion object {
    const val USER_ACCOUNTS = "userAccounts"
    const val PROJECTS = "projects"
    const val PROJECT_PERMISSIONS = "projectPermissions"
    const val RATE_LIMITS = "rateLimits"
    const val MACHINE_TRANSLATIONS = "machineTranslations"
    const val PROJECT_TRANSLATIONS_MODIFIED = "projectTranslationsModified"

    val caches = listOf(
      USER_ACCOUNTS,
      PROJECTS,
      PROJECT_PERMISSIONS,
      MACHINE_TRANSLATIONS,
      PROJECT_TRANSLATIONS_MODIFIED
    )
  }
}
