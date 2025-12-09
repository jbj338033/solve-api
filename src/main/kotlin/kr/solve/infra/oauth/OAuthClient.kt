package kr.solve.infra.oauth

interface OAuthClient {
    suspend fun getUserInfo(credential: String): OAuthUserInfo
}
