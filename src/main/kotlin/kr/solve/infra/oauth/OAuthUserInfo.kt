package kr.solve.infra.oauth

import kr.solve.domain.user.domain.enums.UserOAuthProvider

data class OAuthUserInfo(
    val provider: UserOAuthProvider,
    val providerId: String,
    val email: String,
    val name: String,
    val profileImage: String?,
)
