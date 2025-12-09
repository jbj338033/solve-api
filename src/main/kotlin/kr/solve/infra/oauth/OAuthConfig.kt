package kr.solve.infra.oauth

import kr.solve.domain.user.domain.enums.UserOAuthProvider
import kr.solve.infra.oauth.github.GitHubOAuthClient
import kr.solve.infra.oauth.google.GoogleOAuthClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OAuthConfig(
    private val gitHubOAuthClient: GitHubOAuthClient,
    private val googleOAuthClient: GoogleOAuthClient,
) {
    @Bean
    fun oauthClients(): Map<UserOAuthProvider, OAuthClient> =
        mapOf(
            UserOAuthProvider.GITHUB to gitHubOAuthClient,
            UserOAuthProvider.GOOGLE to googleOAuthClient,
        )
}
