package kr.solve.domain.user.domain.repository

import kr.solve.domain.user.domain.entity.UserOAuth
import kr.solve.domain.user.domain.enums.UserOAuthProvider
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface UserOAuthRepository : CoroutineCrudRepository<UserOAuth, Long> {
    suspend fun findByProviderAndProviderId(
        provider: UserOAuthProvider,
        providerId: String,
    ): UserOAuth?

    suspend fun findAllByUserId(userId: Long): List<UserOAuth>

    suspend fun countByUserId(userId: Long): Long

    suspend fun deleteByUserIdAndProvider(
        userId: Long,
        provider: UserOAuthProvider,
    )
}
