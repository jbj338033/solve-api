package kr.solve.domain.banner.domain.repository

import kotlinx.coroutines.flow.Flow
import kr.solve.domain.banner.domain.entity.UserBanner
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface UserBannerRepository : CoroutineCrudRepository<UserBanner, Long> {
    fun findAllByUserId(userId: Long): Flow<UserBanner>

    suspend fun existsByUserIdAndBannerId(
        userId: Long,
        bannerId: Long,
    ): Boolean
}
