package kr.solve.domain.submission.domain.repository

import kotlinx.coroutines.flow.Flow
import kr.solve.domain.submission.domain.entity.Submission
import kr.solve.domain.submission.domain.enums.JudgeResult
import kr.solve.domain.submission.domain.enums.SubmissionStatus
import org.springframework.data.r2dbc.repository.Modifying
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface SubmissionRepository : CoroutineCrudRepository<Submission, Long> {
    suspend fun countByUserId(userId: Long): Long

    @Query("SELECT DISTINCT problem_id FROM submissions WHERE user_id = :userId AND result = 'ACCEPTED'")
    fun findSolvedProblemIdsByUserId(userId: Long): Flow<Long>

    @Query("SELECT DISTINCT problem_id FROM submissions WHERE user_id = :userId AND problem_id = ANY(:problemIds) AND result = 'ACCEPTED'")
    fun findSolvedProblemIdsByUserIdAndProblemIds(userId: Long, problemIds: Array<Long>): Flow<Long>

    @Query("SELECT DISTINCT problem_id FROM submissions WHERE user_id = :userId AND problem_id = ANY(:problemIds)")
    fun findAttemptedProblemIdsByUserIdAndProblemIds(userId: Long, problemIds: Array<Long>): Flow<Long>

    suspend fun existsByUserIdAndProblemIdAndResult(
        userId: Long,
        problemId: Long,
        result: JudgeResult,
    ): Boolean

    suspend fun existsByUserIdAndProblemId(
        userId: Long,
        problemId: Long,
    ): Boolean

    @Query("SELECT * FROM submissions WHERE (:cursor IS NULL OR id < :cursor) ORDER BY id DESC LIMIT :limit")
    fun findAllByOrderByIdDesc(
        cursor: Long?,
        limit: Int,
    ): Flow<Submission>

    @Query("SELECT * FROM submissions WHERE problem_id = :problemId AND (:cursor IS NULL OR id < :cursor) ORDER BY id DESC LIMIT :limit")
    fun findAllByProblemIdOrderByIdDesc(
        problemId: Long,
        cursor: Long?,
        limit: Int,
    ): Flow<Submission>

    @Query("SELECT * FROM submissions WHERE user_id = :userId AND (:cursor IS NULL OR id < :cursor) ORDER BY id DESC LIMIT :limit")
    fun findAllByUserIdOrderByIdDesc(
        userId: Long,
        cursor: Long?,
        limit: Int,
    ): Flow<Submission>

    fun findAllByUserIdAndProblemId(
        userId: Long,
        problemId: Long,
    ): Flow<Submission>

    @Modifying
    @Query("UPDATE submissions SET status = :status WHERE id = :id")
    suspend fun updateStatus(
        id: Long,
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
        id: Long,
        status: SubmissionStatus,
        result: JudgeResult,
        score: Int,
        time: Int,
        memory: Int,
        error: String?,
    )
}
