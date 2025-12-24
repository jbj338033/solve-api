package kr.solve.domain.review.application.service

import kotlinx.coroutines.flow.toList
import kr.solve.common.pagination.CursorPage
import kr.solve.domain.problem.domain.enums.ProblemStatus
import kr.solve.domain.problem.domain.error.ProblemError
import kr.solve.domain.problem.domain.repository.ProblemRepository
import kr.solve.domain.review.domain.entity.ReviewComment
import kr.solve.domain.review.domain.enums.ReviewStatus
import kr.solve.domain.review.domain.error.ReviewError
import kr.solve.domain.review.domain.repository.ProblemReviewRepository
import kr.solve.domain.review.domain.repository.ReviewCommentRepository
import kr.solve.domain.review.presentation.request.CreateCommentRequest
import kr.solve.domain.review.presentation.request.ReviewDecisionRequest
import kr.solve.domain.review.presentation.response.ReviewResponse
import kr.solve.domain.user.domain.repository.UserRepository
import kr.solve.global.error.BusinessException
import kr.solve.global.security.userId
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class AdminReviewService(
    private val problemRepository: ProblemRepository,
    private val problemReviewRepository: ProblemReviewRepository,
    private val reviewCommentRepository: ReviewCommentRepository,
    private val userRepository: UserRepository,
) {
    suspend fun getPendingReviews(cursor: Long?, limit: Int): CursorPage<ReviewResponse.Summary> {
        val reviews = problemReviewRepository
            .findAllByStatusWithCursor(ReviewStatus.PENDING.name, cursor, limit + 1)
            .toList()

        val userIds = (reviews.map { it.requesterId } + reviews.mapNotNull { it.reviewerId }).distinct()
        val userMap = userRepository.findAllByIdIn(userIds).toList().associateBy { it.id }

        return CursorPage.of(reviews, limit) { review ->
            ReviewResponse.Summary(
                id = review.id!!,
                problemId = review.problemId,
                requester = userMap[review.requesterId]!!.toAuthor(),
                reviewer = review.reviewerId?.let { userMap[it]?.toAuthor() },
                status = review.status,
                createdAt = review.createdAt,
                reviewedAt = review.reviewedAt,
            )
        }
    }

    suspend fun getReview(reviewId: Long): ReviewResponse.Detail {
        val review = problemReviewRepository.findById(reviewId)
            ?: throw BusinessException(ReviewError.NotFound)

        val comments = reviewCommentRepository.findAllByReviewIdOrderByCreatedAtAsc(reviewId).toList()
        val userIds = (listOf(review.requesterId) + listOfNotNull(review.reviewerId) + comments.map { it.authorId }).distinct()
        val userMap = userRepository.findAllByIdIn(userIds).toList().associateBy { it.id }

        return ReviewResponse.Detail(
            id = review.id!!,
            problemId = review.problemId,
            requester = userMap[review.requesterId]!!.toAuthor(),
            reviewer = review.reviewerId?.let { userMap[it]?.toAuthor() },
            status = review.status,
            summary = review.summary,
            createdAt = review.createdAt,
            reviewedAt = review.reviewedAt,
            comments = comments.map { comment ->
                ReviewResponse.Comment(
                    id = comment.id!!,
                    author = userMap[comment.authorId]!!.toAuthor(),
                    content = comment.content,
                    createdAt = comment.createdAt,
                    updatedAt = comment.updatedAt,
                )
            },
        )
    }

    @Transactional
    suspend fun approve(reviewId: Long, request: ReviewDecisionRequest): ReviewResponse.Summary {
        val review = problemReviewRepository.findById(reviewId)
            ?: throw BusinessException(ReviewError.NotFound)

        if (review.status != ReviewStatus.PENDING) {
            throw BusinessException(ReviewError.AlreadyReviewed)
        }

        val problem = problemRepository.findById(review.problemId)
            ?: throw BusinessException(ProblemError.NotFound)

        val updatedReview = problemReviewRepository.save(
            review.copy(
                status = ReviewStatus.APPROVED,
                reviewerId = userId(),
                summary = request.summary,
                reviewedAt = LocalDateTime.now(),
            ),
        )

        problemRepository.save(
            problem.copy(
                status = ProblemStatus.APPROVED,
                isPublic = true,
            ),
        )

        val userMap = userRepository.findAllByIdIn(listOf(updatedReview.requesterId, userId())).toList().associateBy { it.id }

        return ReviewResponse.Summary(
            id = updatedReview.id!!,
            problemId = updatedReview.problemId,
            requester = userMap[updatedReview.requesterId]!!.toAuthor(),
            reviewer = userMap[userId()]?.toAuthor(),
            status = updatedReview.status,
            createdAt = updatedReview.createdAt,
            reviewedAt = updatedReview.reviewedAt,
        )
    }

    @Transactional
    suspend fun requestChanges(reviewId: Long, request: ReviewDecisionRequest): ReviewResponse.Summary {
        val review = problemReviewRepository.findById(reviewId)
            ?: throw BusinessException(ReviewError.NotFound)

        if (review.status != ReviewStatus.PENDING) {
            throw BusinessException(ReviewError.AlreadyReviewed)
        }

        val problem = problemRepository.findById(review.problemId)
            ?: throw BusinessException(ProblemError.NotFound)

        val updatedReview = problemReviewRepository.save(
            review.copy(
                status = ReviewStatus.CHANGES_REQUESTED,
                reviewerId = userId(),
                summary = request.summary,
                reviewedAt = LocalDateTime.now(),
            ),
        )

        problemRepository.save(problem.copy(status = ProblemStatus.DRAFT))

        val userMap = userRepository.findAllByIdIn(listOf(updatedReview.requesterId, userId())).toList().associateBy { it.id }

        return ReviewResponse.Summary(
            id = updatedReview.id!!,
            problemId = updatedReview.problemId,
            requester = userMap[updatedReview.requesterId]!!.toAuthor(),
            reviewer = userMap[userId()]?.toAuthor(),
            status = updatedReview.status,
            createdAt = updatedReview.createdAt,
            reviewedAt = updatedReview.reviewedAt,
        )
    }

    @Transactional
    suspend fun createComment(reviewId: Long, request: CreateCommentRequest): ReviewResponse.Comment {
        val review = problemReviewRepository.findById(reviewId)
            ?: throw BusinessException(ReviewError.NotFound)

        val comment = reviewCommentRepository.save(
            ReviewComment(
                reviewId = reviewId,
                authorId = userId(),
                content = request.content,
            ),
        )

        val author = userRepository.findById(comment.authorId)!!

        return ReviewResponse.Comment(
            id = comment.id!!,
            author = author.toAuthor(),
            content = comment.content,
            createdAt = comment.createdAt,
            updatedAt = comment.updatedAt,
        )
    }

    private fun kr.solve.domain.user.domain.entity.User.toAuthor() = ReviewResponse.Author(
        id = id!!,
        username = username,
        displayName = displayName,
        profileImage = profileImage,
    )
}
