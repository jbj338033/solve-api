package kr.solve.domain.auth.application.service

import kr.solve.domain.auth.domain.error.AuthError
import kr.solve.domain.auth.domain.repository.RefreshTokenRepository
import kr.solve.domain.auth.presentation.request.OAuthLoginRequest
import kr.solve.domain.auth.presentation.request.RefreshRequest
import kr.solve.domain.auth.presentation.response.LoginResponse
import kr.solve.domain.auth.presentation.response.RefreshResponse
import kr.solve.domain.user.domain.entity.User
import kr.solve.domain.user.domain.entity.UserOAuth
import kr.solve.domain.user.domain.enums.UserOAuthProvider
import kr.solve.domain.user.domain.enums.UserRole
import kr.solve.domain.user.domain.error.UserError
import kr.solve.domain.user.domain.repository.UserOAuthRepository
import kr.solve.domain.user.domain.repository.UserRepository
import kr.solve.domain.user.presentation.response.toMe
import kr.solve.global.error.BusinessException
import kr.solve.global.security.jwt.JwtProvider
import kr.solve.global.security.jwt.JwtProvider.JwtType
import kr.solve.infra.oauth.OAuthClient
import kr.solve.infra.oauth.OAuthUserInfo
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val userOAuthRepository: UserOAuthRepository,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val jwtProvider: JwtProvider,
    private val oauthClients: Map<UserOAuthProvider, OAuthClient>,
) {
    @Transactional
    suspend fun login(
        provider: UserOAuthProvider,
        request: OAuthLoginRequest,
    ): LoginResponse {
        val client = oauthClients[provider] ?: throw BusinessException(AuthError.UNSUPPORTED_PROVIDER)
        val info = client.getUserInfo(request.credential)

        val oauth = userOAuthRepository.findByProviderAndProviderId(provider, info.providerId)
        val user =
            if (oauth != null) {
                userRepository.findById(oauth.userId) ?: throw BusinessException(UserError.NOT_FOUND)
            } else {
                register(provider, info)
            }

        val providers = userOAuthRepository.findAllByUserId(user.id!!).map { it.provider }
        val accessToken = jwtProvider.createAccessToken(user.id!!, user.role)
        val refreshToken = jwtProvider.createRefreshToken(user.id!!)
        refreshTokenRepository.save(refreshToken, user.id!!)

        return LoginResponse(accessToken, refreshToken, user.toMe(providers))
    }

    suspend fun refresh(request: RefreshRequest): RefreshResponse {
        val token = request.refreshToken

        if (!jwtProvider.validateToken(token) || jwtProvider.getType(token) != JwtType.REFRESH) {
            throw BusinessException(AuthError.INVALID_REFRESH_TOKEN)
        }

        val userId =
            refreshTokenRepository.findUserIdByToken(token)
                ?: throw BusinessException(AuthError.INVALID_REFRESH_TOKEN)

        val user =
            userRepository.findById(userId)
                ?: throw BusinessException(UserError.NOT_FOUND)

        refreshTokenRepository.deleteByToken(token)

        val accessToken = jwtProvider.createAccessToken(userId, user.role)
        val refreshToken = jwtProvider.createRefreshToken(userId)
        refreshTokenRepository.save(refreshToken, userId)

        return RefreshResponse(accessToken, refreshToken)
    }

    suspend fun logout(request: RefreshRequest) {
        refreshTokenRepository.deleteByToken(request.refreshToken)
    }

    private suspend fun register(
        provider: UserOAuthProvider,
        info: OAuthUserInfo,
    ): User {
        val user =
            userRepository.save(
                User(
                    username = generateUsername(info.name),
                    displayName = info.name,
                    email = info.email,
                    profileImage = info.profileImage.orEmpty(),
                    bio = "",
                    organization = "",
                    problemRating = 0,
                    contestRating = 0,
                    role = UserRole.USER,
                ),
            )
        userOAuthRepository.save(UserOAuth(userId = user.id!!, provider = provider, providerId = info.providerId))
        return user
    }

    private suspend fun generateUsername(name: String): String {
        val base =
            name
                .lowercase()
                .replace(Regex("[^a-z0-9]"), "")
                .take(20)
                .ifEmpty { "user" }
        var username = base
        var count = 0
        while (userRepository.existsByUsername(username)) {
            username = "${base.take(17)}${++count}"
        }
        return username
    }
}
