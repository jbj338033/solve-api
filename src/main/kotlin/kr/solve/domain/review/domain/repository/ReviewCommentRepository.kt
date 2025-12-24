package kr.solve.domain.review.domain.repository

import kotlinx.coroutines.flow.Flow
import kr.solve.domain.review.domain.entity.ReviewComment
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface ReviewCommentRepository : CoroutineCrudRepository<ReviewComment, Long> {
    fun findAllByReviewIdOrderByCreatedAtAsc(reviewId: Long): Flow<ReviewComment>
    suspend fun deleteAllByReviewId(reviewId: Long)
}
