package io.tolgee.fixtures

import io.tolgee.constants.ApiScope
import io.tolgee.dtos.response.ApiKeyDTO.ApiKeyDTO
import io.tolgee.model.UserAccount
import io.tolgee.service.ApiKeyService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import org.springframework.test.web.servlet.ResultActions

@Component
@Scope("prototype")
class RepositoryApiKeyAuthRequestPerformer(
        //bean is created manually
        @Suppress("SpringJavaInjectionPointsAutowiringInspection") private val userAccount: UserAccount,
        //bean is created manually
        @Suppress("SpringJavaInjectionPointsAutowiringInspection") private val scopes: Array<ApiScope>,
) : RepositoryAuthRequestPerformer(userAccount) {

    @field:Autowired
    lateinit var apiKeyService: ApiKeyService

    val apiKey: ApiKeyDTO by lazy {
        apiKeyService.createApiKey(userAccount, scopes = this.scopes.toSet(), project)
    }

    override fun performRepositoryAuthPut(url: String, content: Any?): ResultActions {
        return performPut(API_REPOSITORY_URL_PREFIX + url.withApiKey, content)
    }

    override fun performRepositoryAuthPost(url: String, content: Any?): ResultActions {
        return performPost(API_REPOSITORY_URL_PREFIX + url.withApiKey, content)
    }

    override fun performRepositoryAuthGet(url: String): ResultActions {
        return performGet(API_REPOSITORY_URL_PREFIX + url.withApiKey)
    }

    override fun performRepositoryAuthDelete(url: String, content: Any?): ResultActions {
        return performDelete(API_REPOSITORY_URL_PREFIX + url.withApiKey, content)
    }

    private val String.withApiKey: String
        get() {
            val symbol = if (this.contains("?")) "&" else "?"
            return this + symbol + "ak=" + apiKey.key
        }
}
