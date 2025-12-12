package kr.solve.domain.user.domain.repository

import kotlinx.coroutines.flow.Flow
import kr.solve.domain.user.domain.entity.UserRatingHistory
import kr.solve.domain.user.domain.enums.RatingType
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface UserRatingHistoryRepository : CoroutineCrudRepository<UserRatingHistory, Long> {
    fun findAllByUserIdOrderByRecordedAtDesc(userId: Long): Flow<UserRatingHistory>

    fun findAllByUserIdAndRatingTypeOrderByRecordedAtDesc(
        userId: Long,
        ratingType: RatingType,
    ): Flow<UserRatingHistory>
}
