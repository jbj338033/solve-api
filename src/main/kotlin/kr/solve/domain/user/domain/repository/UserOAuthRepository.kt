package kr.solve.domain.user.domain.repository

import kr.solve.domain.user.domain.entity.UserOAuth
import kr.solve.domain.user.domain.enums.UserOAuthProvider
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import java.util.UUID

interface UserOAuthRepository : CoroutineCrudRepository<UserOAuth, UUID> {
    suspend fun findByProviderAndProviderId(
        provider: UserOAuthProvider,
        providerId: String,
    ): UserOAuth?

    suspend fun findAllByUserId(userId: UUID): List<UserOAuth>

    suspend fun countByUserId(userId: UUID): Long

    suspend fun deleteByUserIdAndProvider(
        userId: UUID,
        provider: UserOAuthProvider,
    )
}
