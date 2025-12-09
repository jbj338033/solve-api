package kr.solve.domain.user.domain.repository

import kotlinx.coroutines.flow.Flow
import kr.solve.domain.user.domain.entity.User
import org.springframework.data.r2dbc.repository.Modifying
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import java.time.LocalDate
import java.util.UUID

interface UserRepository : CoroutineCrudRepository<User, UUID> {
    suspend fun findByUsername(username: String): User?

    suspend fun existsByUsername(username: String): Boolean

    fun findAllByIdIn(ids: List<UUID>): Flow<User>

    fun findAllByOrderByProblemRatingDesc(): Flow<User>

    fun findAllByOrderByContestRatingDesc(): Flow<User>

    @Modifying
    @Query(
        """
        UPDATE users SET
            current_streak = CASE
                WHEN last_solved_date = :yesterday THEN current_streak + 1
                WHEN last_solved_date = :today THEN current_streak
                ELSE 1
            END,
            max_streak = GREATEST(max_streak, CASE
                WHEN last_solved_date = :yesterday THEN current_streak + 1
                WHEN last_solved_date = :today THEN current_streak
                ELSE 1
            END),
            last_solved_date = :today,
            updated_at = NOW()
        WHERE id = :userId
        """,
    )
    suspend fun updateStreak(
        userId: UUID,
        today: LocalDate,
        yesterday: LocalDate,
    )
}
