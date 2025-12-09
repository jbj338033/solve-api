package kr.solve.domain.submission.domain.repository

import kotlinx.coroutines.flow.Flow
import kr.solve.domain.submission.domain.entity.Submission
import kr.solve.domain.submission.domain.enums.JudgeResult
import kr.solve.domain.submission.domain.enums.SubmissionStatus
import org.springframework.data.r2dbc.repository.Modifying
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import java.util.UUID

interface SubmissionRepository : CoroutineCrudRepository<Submission, UUID> {
    suspend fun countByUserId(userId: UUID): Long

    @Query("SELECT DISTINCT problem_id FROM submissions WHERE user_id = :userId AND result = 'ACCEPTED'")
    fun findSolvedProblemIdsByUserId(userId: UUID): Flow<UUID>

    suspend fun existsByUserIdAndProblemIdAndResult(
        userId: UUID,
        problemId: UUID,
        result: JudgeResult,
    ): Boolean

    @Query("SELECT * FROM submissions WHERE (:cursor IS NULL OR id < :cursor) ORDER BY id DESC LIMIT :limit")
    fun findAllByOrderByIdDesc(
        cursor: UUID?,
        limit: Int,
    ): Flow<Submission>

    @Query("SELECT * FROM submissions WHERE problem_id = :problemId AND (:cursor IS NULL OR id < :cursor) ORDER BY id DESC LIMIT :limit")
    fun findAllByProblemIdOrderByIdDesc(
        problemId: UUID,
        cursor: UUID?,
        limit: Int,
    ): Flow<Submission>

    @Query("SELECT * FROM submissions WHERE user_id = :userId AND (:cursor IS NULL OR id < :cursor) ORDER BY id DESC LIMIT :limit")
    fun findAllByUserIdOrderByIdDesc(
        userId: UUID,
        cursor: UUID?,
        limit: Int,
    ): Flow<Submission>

    fun findAllByUserIdAndProblemId(
        userId: UUID,
        problemId: UUID,
    ): Flow<Submission>

    @Modifying
    @Query("UPDATE submissions SET status = :status WHERE id = :id")
    suspend fun updateStatus(
        id: UUID,
        status: SubmissionStatus,
    )

    @Modifying
    @Query(
        """
        UPDATE submissions
        SET status = :status, result = :result, score = :score,
            time_used = :time, memory_used = :memory, error = :error, judged_at = NOW()
        WHERE id = :id
        """,
    )
    suspend fun updateResult(
        id: UUID,
        status: SubmissionStatus,
        result: JudgeResult,
        score: Int,
        time: Int,
        memory: Int,
        error: String?,
    )
}
