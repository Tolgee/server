package io.tolgee.configuration.tolgee

import io.tolgee.configuration.annotations.DocProperty
import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "tolgee.import")
@DocProperty(
  description = "Bulk-imports exported json files in the database during startup. " +
    "Useful to quickly provision a development server, and used for testing.",
  displayName = "Import"
)
class ImportProperties {
  @DocProperty(description = "File path of the directory where the file to import are located.")
  var dir: String? = null

  @DocProperty(
    description = "Whether an implicit API key should be created.\n" +
      "\n" +
      "The key is built with a predictable format: " +
      "`\${lowercase filename (without extension)}-\${initial username}-imported-project-implicit`\n" +
      "\n" +
      ":::danger\n" +
      "While this is useful for tests, make sure to keep this **disabled** " +
      "if you're importing projects on a production server as trying this predictable key " +
      "may be the first thing an attacker will attempt to gain unauthorized access!\n" +
      ":::\n\n"
  )
  var createImplicitApiKey: Boolean = false

  @DocProperty(description = "The language tag of the base language of the imported projects.")
  var baseLanguageTag: String = "en"
}
