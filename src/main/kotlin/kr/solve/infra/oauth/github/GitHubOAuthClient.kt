package kr.solve.infra.oauth.github

import com.fasterxml.jackson.annotation.JsonProperty
import kr.solve.domain.auth.domain.error.AuthError
import kr.solve.domain.user.domain.enums.UserOAuthProvider
import kr.solve.global.error.BusinessException
import kr.solve.infra.oauth.OAuthClient
import kr.solve.infra.oauth.OAuthProperties
import kr.solve.infra.oauth.OAuthUserInfo
import org.springframework.http.HttpStatusCode
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import reactor.core.publisher.Mono

@Component
class GitHubOAuthClient(
    private val oAuthProperties: OAuthProperties,
) : OAuthClient {
    private val webClient =
        WebClient
            .builder()
            .baseUrl("https://github.com")
            .build()

    private val apiClient =
        WebClient
            .builder()
            .baseUrl("https://api.github.com")
            .build()

    override suspend fun getUserInfo(credential: String): OAuthUserInfo {
        val accessToken = getAccessToken(credential)
        return fetchUserInfo(accessToken)
    }

    private suspend fun getAccessToken(code: String): String {
        val response =
            webClient
                .post()
                .uri("/login/oauth/access_token")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(
                    GitHubTokenRequest(
                        clientId = oAuthProperties.github.clientId,
                        clientSecret = oAuthProperties.github.clientSecret,
                        code = code,
                    ),
                ).retrieve()
                .onStatus(HttpStatusCode::isError) {
                    Mono.error(BusinessException(AuthError.OAuthFailed("GitHub")))
                }.awaitBody<GitHubTokenResponse>()

        if (response.accessToken == null) {
            throw BusinessException(AuthError.OAuthFailed("GitHub"))
        }

        return response.accessToken
    }

    private suspend fun fetchUserInfo(accessToken: String): OAuthUserInfo {
        val userResponse =
            apiClient
                .get()
                .uri("/user")
                .header("Authorization", "Bearer $accessToken")
                .retrieve()
                .onStatus(HttpStatusCode::isError) {
                    Mono.error(BusinessException(AuthError.OAuthFailed("GitHub")))
                }.awaitBody<GitHubUserResponse>()

        val email = userResponse.email ?: fetchPrimaryEmail(accessToken)

        return OAuthUserInfo(
            provider = UserOAuthProvider.GITHUB,
            providerId = userResponse.id.toString(),
            email = email,
            name = userResponse.name ?: userResponse.login,
            profileImage = userResponse.avatarUrl,
        )
    }

    private suspend fun fetchPrimaryEmail(accessToken: String): String {
        val emails =
            apiClient
                .get()
                .uri("/user/emails")
                .header("Authorization", "Bearer $accessToken")
                .retrieve()
                .onStatus(HttpStatusCode::isError) {
                    Mono.error(BusinessException(AuthError.OAuthFailed("GitHub")))
                }.awaitBody<List<GitHubEmailResponse>>()

        return emails.firstOrNull { it.primary }?.email
            ?: emails.firstOrNull()?.email
            ?: throw BusinessException(AuthError.OAuthFailed("GitHub - no email"))
    }
}

private data class GitHubTokenRequest(
    @JsonProperty("client_id")
    val clientId: String,
    @JsonProperty("client_secret")
    val clientSecret: String,
    val code: String,
)

private data class GitHubTokenResponse(
    @JsonProperty("access_token")
    val accessToken: String?,
    @JsonProperty("token_type")
    val tokenType: String?,
    val scope: String?,
)

private data class GitHubUserResponse(
    val id: Long,
    val login: String,
    val name: String?,
    val email: String?,
    @JsonProperty("avatar_url")
    val avatarUrl: String?,
)

private data class GitHubEmailResponse(
    val email: String,
    val primary: Boolean,
    val verified: Boolean,
)
