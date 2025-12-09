package kr.solve.domain.banner.domain.repository

import kotlinx.coroutines.flow.Flow
import kr.solve.domain.banner.domain.entity.UserBanner
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import java.util.UUID

interface UserBannerRepository : CoroutineCrudRepository<UserBanner, UUID> {
    fun findAllByUserId(userId: UUID): Flow<UserBanner>

    suspend fun existsByUserIdAndBannerId(
        userId: UUID,
        bannerId: UUID,
    ): Boolean
}
