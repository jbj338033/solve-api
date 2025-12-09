package kr.solve.infra.oauth

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "oauth")
data class OAuthProperties(
    val github: GitHubProperties,
    val google: GoogleProperties,
) {
    data class GitHubProperties(
        val clientId: String,
        val clientSecret: String,
    )

    data class GoogleProperties(
        val clientId: String,
        val clientSecret: String,
    )
}
