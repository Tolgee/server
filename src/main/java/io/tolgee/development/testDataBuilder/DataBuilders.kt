package io.tolgee.development.testDataBuilder

import io.tolgee.model.*
import io.tolgee.model.dataImport.*

typealias FT<T> = T.() -> Unit

class DataBuilders {
    class RepositoryBuilder(userOwner: UserAccount? = null,
                            organizationOwner: Organization? = null,
                            testDataBuilder: TestDataBuilder
    ) : BaseEntityDataBuilder<Repository>() {
        override var self: Repository = Repository().apply {
            if (userOwner == null && organizationOwner == null) {
                if (testDataBuilder.data.userAccounts.size > 0) {
                    this.userOwner = testDataBuilder.data.userAccounts.first()
                } else if (testDataBuilder.data.organizations.size > 0) {
                    this.organizationOwner = testDataBuilder.data.organizations.first().self
                }
                return@apply
            }

            this.userOwner = userOwner
            this.organizationOwner = organizationOwner
        }

        class DATA {
            val languages = mutableListOf<LanguageBuilder>()
            val imports = mutableListOf<ImportBuilder>()
            val keys = mutableListOf<KeyBuilder>()
            val translations = mutableListOf<TranslationBuilder>()
        }

        var data = DATA()

        fun addImport(author: UserAccount? = null, ft: FT<ImportBuilder>) =
                addOperation(data.imports, ImportBuilder(this, author), ft)

        fun addLanguage(ft: FT<LanguageBuilder>) =
                addOperation(data.languages, ft)

        fun addKey(ft: FT<KeyBuilder>) = addOperation(data.keys, ft)

        fun addTranslation(ft: FT<TranslationBuilder>) = addOperation(data.translations, ft)
    }

    class ImportBuilder(
            repositoryBuilder: RepositoryBuilder,
            author: UserAccount? = null
    ) : BaseEntityDataBuilder<Import>() {
        class DATA {
            val importFiles = mutableListOf<ImportFileBuilder>()
        }

        val data = DATA()

        override var self: Import = Import(author ?: repositoryBuilder.self.userOwner!!, repositoryBuilder.self)

        fun addImportFile(ft: FT<ImportFileBuilder>) = addOperation(data.importFiles, ft)
    }

    class ImportFileBuilder(importBuilder: ImportBuilder) : BaseEntityDataBuilder<ImportFile>() {
        override var self: ImportFile = ImportFile("lang.json", importBuilder.self)

        class DATA {
            val importKeys = mutableListOf<ImportKeyBuilder>()
            val importLanguages = mutableListOf<ImportLanguageBuilder>()
            val importTranslations = mutableListOf<ImportTranslationBuilder>()
        }

        val data = DATA()

        fun addImportKey(ft: FT<ImportKeyBuilder>) = addOperation(data.importKeys, ft)
        fun addImportLanguage(ft: FT<ImportLanguageBuilder>) = addOperation(data.importLanguages, ft)
        fun addImportTranslation(ft: FT<ImportTranslationBuilder>) = addOperation(data.importTranslations, ft)
    }

    class ImportKeyBuilder(
            importFileBuilder: ImportFileBuilder
    ) : EntityDataBuilder<ImportKey> {
        override var self: ImportKey = ImportKey("testKey").apply {
            files.add(importFileBuilder.self)
        }

    }

    class ImportLanguageBuilder(
            importFileBuilder: ImportFileBuilder
    ) : EntityDataBuilder<ImportLanguage> {
        override var self: ImportLanguage = ImportLanguage("en", importFileBuilder.self)

    }

    class ImportTranslationBuilder(
            importFileBuilder: ImportFileBuilder
    ) : EntityDataBuilder<ImportTranslation> {
        override var self: ImportTranslation =
                ImportTranslation("test translation", importFileBuilder.data.importLanguages[0].self).apply {
                    key = importFileBuilder.data.importKeys.first().self
                }
    }

    class OrganizationBuilder(
            testDataBuilder: TestDataBuilder
    ) : EntityDataBuilder<Organization> {
        override var self: Organization = Organization()
    }

    class KeyBuilder(
            repositoryBuilder: RepositoryBuilder
    ) : EntityDataBuilder<Key> {
        override var self: Key = Key()
    }

    class LanguageBuilder(
            repositoryBuilder: RepositoryBuilder
    ) : EntityDataBuilder<Language> {
        override var self: Language = Language()
    }

    class TranslationBuilder(
            repositoryBuilder: RepositoryBuilder
    ) : EntityDataBuilder<Translation> {
        override var self: Translation = Translation()
    }
}
