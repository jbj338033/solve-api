package kr.solve.domain.review.domain.repository

import kotlinx.coroutines.flow.Flow
import kr.solve.domain.review.domain.entity.ProblemReview
import kr.solve.domain.review.domain.enums.ReviewStatus
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface ProblemReviewRepository : CoroutineCrudRepository<ProblemReview, Long> {
    fun findAllByProblemIdOrderByCreatedAtDesc(problemId: Long): Flow<ProblemReview>

    fun findAllByStatusOrderByCreatedAtAsc(status: ReviewStatus): Flow<ProblemReview>

    @Query(
        """
        SELECT * FROM problem_reviews
        WHERE status = :status AND (:cursor IS NULL OR id > :cursor)
        ORDER BY id ASC
        LIMIT :limit
        """,
    )
    fun findAllByStatusWithCursor(status: String, cursor: Long?, limit: Int): Flow<ProblemReview>

    suspend fun findFirstByProblemIdOrderByCreatedAtDesc(problemId: Long): ProblemReview?
}
