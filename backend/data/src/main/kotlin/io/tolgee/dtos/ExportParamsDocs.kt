package io.tolgee.dtos

object ExportParamsDocs {
  const val LANGUAGES_DESCRIPTION = """Languages to be contained in export.
                
If null, all languages are exported"""

  const val LANGUAGES_EXAMPLE = "en"

  const val FORMAT_DESCRIPTION = """Format to export to"""

  const val STRUCTURE_DELIMITER_DESCRIPTION = """Delimiter to structure file content. 

e.g. For key "home.header.title" would result in {"home": {"header": "title": {"Hello"}}} structure.

When null, resulting file won't be structured. Works only for generic structured formats (e.g. JSON, YAML), 
specific formats like `YAML_RUBY` don't honor this parameter."""

  const val SUPPORT_ARRAYS_DESCRIPTION = """If true, for structured formats (like JSON) arrays are supported. 

e.g. Key hello[0] will be exported as {"hello": ["..."]}"""

  const val FILTER_KEY_ID_DESCRIPTION = """Filter key IDs to be contained in export"""

  const val FILTER_KEY_ID_NOT_DESCRIPTION = """Filter key IDs not to be contained in export"""

  const val FILTER_TAG_DESCRIPTION = """Filter keys tagged by"""

  const val FILTER_KEY_PREFIX_DESCRIPTION = """Filter keys with prefix"""

  const val FILTER_STATE_DESCRIPTION =
    """Filter translations with state. By default, all states except untranslated is exported."""

  const val FILTER_NAMESPACE_DESCRIPTION =
    "Filter translations with namespace. " +
      "By default, all namespaces everything are exported."

  const val MESSAGE_FORMAT_DESCRIPTION = """Message format to be used for export.
      
e.g. PHP_PO: Hello %s, ICU: Hello {name}. 

This property is honored only for generic formats like JSON or YAML. 
For specific formats like `YAML_RUBY` it's ignored."""

  const val ZIP_DESCRIPTION = """If false, it doesn't return zip of files, but it returns single file.
      
This is possible only when single language is exported. Otherwise it returns "400 - Bad Request" response."""
}
