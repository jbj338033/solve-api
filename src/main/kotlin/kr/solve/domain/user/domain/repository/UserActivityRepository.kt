package kr.solve.domain.user.domain.repository

import kotlinx.coroutines.flow.Flow
import kr.solve.domain.user.domain.entity.UserActivity
import org.springframework.data.r2dbc.repository.Modifying
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import java.time.LocalDate
import java.util.UUID

interface UserActivityRepository : CoroutineCrudRepository<UserActivity, UUID> {
    suspend fun findByUserIdAndDate(
        userId: UUID,
        date: LocalDate,
    ): UserActivity?

    fun findAllByUserIdAndDateBetween(
        userId: UUID,
        startDate: LocalDate,
        endDate: LocalDate,
    ): Flow<UserActivity>

    @Modifying
    @Query(
        """
        INSERT INTO user_activities (id, version, created_at, updated_at, user_id, date, solved_count, submission_count)
        VALUES (gen_random_uuid(), 0, NOW(), NOW(), :userId, :date, :solvedCount, :submissionCount)
        ON CONFLICT (user_id, date) DO UPDATE SET
            solved_count = user_activities.solved_count + :solvedCount,
            submission_count = user_activities.submission_count + :submissionCount,
            updated_at = NOW()
        """,
    )
    suspend fun upsertActivity(
        userId: UUID,
        date: LocalDate,
        solvedCount: Int,
        submissionCount: Int,
    )
}
