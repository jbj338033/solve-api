package kr.solve.infra.oauth.google

import com.fasterxml.jackson.annotation.JsonProperty
import io.github.oshai.kotlinlogging.KotlinLogging
import kr.solve.domain.auth.domain.error.AuthError
import kr.solve.domain.user.domain.enums.UserOAuthProvider
import kr.solve.global.error.BusinessException
import kr.solve.infra.oauth.OAuthClient
import kr.solve.infra.oauth.OAuthProperties
import kr.solve.infra.oauth.OAuthUserInfo
import org.springframework.http.HttpStatusCode
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Mono

private val logger = KotlinLogging.logger {}

@Component
class GoogleOAuthClient(
    private val oAuthProperties: OAuthProperties,
) : OAuthClient {
    private val webClient =
        WebClient
            .builder()
            .baseUrl("https://oauth2.googleapis.com")
            .build()

    override suspend fun getUserInfo(credential: String): OAuthUserInfo {
        val tokenInfo =
            webClient
                .get()
                .uri("/tokeninfo?id_token=$credential")
                .retrieve()
                .onStatus(HttpStatusCode::isError) { response ->
                    response.bodyToMono<String>().flatMap { body ->
                        logger.error { "Google OAuth failed: ${response.statusCode()} - $body" }
                        Mono.error(BusinessException(AuthError.OAUTH_FAILED, "Google"))
                    }
                }.awaitBody<GoogleTokenInfoResponse>()

        if (tokenInfo.aud != oAuthProperties.google.clientId) {
            throw BusinessException(AuthError.OAUTH_FAILED, "Google - invalid audience")
        }

        return OAuthUserInfo(
            provider = UserOAuthProvider.GOOGLE,
            providerId = tokenInfo.sub,
            email = tokenInfo.email,
            name = tokenInfo.name,
            profileImage = tokenInfo.picture,
        )
    }
}

private data class GoogleTokenInfoResponse(
    val sub: String,
    val email: String,
    val name: String,
    val picture: String?,
    val aud: String,
    @JsonProperty("email_verified")
    val emailVerified: String?,
)
