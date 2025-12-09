package kr.solve.domain.user.domain.repository

import kotlinx.coroutines.flow.Flow
import kr.solve.domain.user.domain.entity.UserRatingHistory
import kr.solve.domain.user.domain.enums.RatingType
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import java.util.UUID

interface UserRatingHistoryRepository : CoroutineCrudRepository<UserRatingHistory, UUID> {
    fun findAllByUserIdOrderByRecordedAtDesc(userId: UUID): Flow<UserRatingHistory>

    fun findAllByUserIdAndRatingTypeOrderByRecordedAtDesc(
        userId: UUID,
        ratingType: RatingType,
    ): Flow<UserRatingHistory>
}
