package kr.solve.domain.review.application.service

import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kr.solve.domain.problem.domain.enums.ProblemStatus
import kr.solve.domain.problem.domain.error.ProblemError
import kr.solve.domain.problem.domain.repository.ProblemRepository
import kr.solve.domain.problem.domain.repository.ProblemSourceRepository
import kr.solve.domain.review.domain.entity.ProblemReview
import kr.solve.domain.review.domain.entity.ReviewComment
import kr.solve.domain.review.domain.enums.ReviewStatus
import kr.solve.domain.review.domain.error.ReviewError
import kr.solve.domain.review.domain.repository.ProblemReviewRepository
import kr.solve.domain.review.domain.repository.ReviewCommentRepository
import kr.solve.domain.review.presentation.request.CreateCommentRequest
import kr.solve.domain.review.presentation.request.CreateReviewRequest
import kr.solve.domain.review.presentation.request.UpdateCommentRequest
import kr.solve.domain.review.presentation.response.ReviewResponse
import kr.solve.domain.user.domain.repository.UserRepository
import kr.solve.global.error.BusinessException
import kr.solve.global.security.userId
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class ReviewService(
    private val problemRepository: ProblemRepository,
    private val problemSourceRepository: ProblemSourceRepository,
    private val problemReviewRepository: ProblemReviewRepository,
    private val reviewCommentRepository: ReviewCommentRepository,
    private val userRepository: UserRepository,
) {
    @Transactional
    suspend fun createReview(problemId: Long, request: CreateReviewRequest): ReviewResponse.Id {
        val problem = problemRepository.findById(problemId)
            ?: throw BusinessException(ProblemError.NotFound)

        if (problem.authorId != userId()) {
            throw BusinessException(ProblemError.AccessDenied)
        }

        if (problem.status != ProblemStatus.DRAFT && problem.status != ProblemStatus.REJECTED) {
            throw BusinessException(ReviewError.CannotRequestReview)
        }

        problemSourceRepository.findByProblemId(problemId)
            ?: throw BusinessException(ProblemError.SolutionNotFound)

        val review = problemReviewRepository.save(
            ProblemReview(
                problemId = problemId,
                requesterId = userId(),
                status = ReviewStatus.PENDING,
            ),
        )

        if (!request.message.isNullOrBlank()) {
            reviewCommentRepository.save(
                ReviewComment(
                    reviewId = review.id!!,
                    authorId = userId(),
                    content = request.message,
                ),
            )
        }

        problemRepository.save(problem.copy(status = ProblemStatus.PENDING))

        return ReviewResponse.Id(review.id!!)
    }

    suspend fun getReviewsByProblemId(problemId: Long): List<ReviewResponse.Summary> {
        val problem = problemRepository.findById(problemId)
            ?: throw BusinessException(ProblemError.NotFound)

        if (problem.authorId != userId()) {
            throw BusinessException(ProblemError.AccessDenied)
        }

        val reviews = problemReviewRepository.findAllByProblemIdOrderByCreatedAtDesc(problemId).toList()
        val userIds = (reviews.map { it.requesterId } + reviews.mapNotNull { it.reviewerId }).distinct()
        val userMap = userRepository.findAllByIdIn(userIds).toList().associateBy { it.id }

        return reviews.map { review ->
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

        val problem = problemRepository.findById(review.problemId)
            ?: throw BusinessException(ProblemError.NotFound)

        if (problem.authorId != userId()) {
            throw BusinessException(ReviewError.AccessDenied)
        }

        return buildReviewDetail(review)
    }

    suspend fun getComments(reviewId: Long): List<ReviewResponse.Comment> {
        val review = problemReviewRepository.findById(reviewId)
            ?: throw BusinessException(ReviewError.NotFound)

        val problem = problemRepository.findById(review.problemId)
            ?: throw BusinessException(ProblemError.NotFound)

        if (problem.authorId != userId()) {
            throw BusinessException(ReviewError.AccessDenied)
        }

        val comments = reviewCommentRepository.findAllByReviewIdOrderByCreatedAtAsc(reviewId).toList()
        val userIds = comments.map { it.authorId }.distinct()
        val userMap = userRepository.findAllByIdIn(userIds).toList().associateBy { it.id }

        return comments.map { comment ->
            ReviewResponse.Comment(
                id = comment.id!!,
                author = userMap[comment.authorId]!!.toAuthor(),
                content = comment.content,
                createdAt = comment.createdAt,
                updatedAt = comment.updatedAt,
            )
        }
    }

    @Transactional
    suspend fun createComment(reviewId: Long, request: CreateCommentRequest): ReviewResponse.Id {
        val review = problemReviewRepository.findById(reviewId)
            ?: throw BusinessException(ReviewError.NotFound)

        val problem = problemRepository.findById(review.problemId)
            ?: throw BusinessException(ProblemError.NotFound)

        if (problem.authorId != userId()) {
            throw BusinessException(ReviewError.AccessDenied)
        }

        val comment = reviewCommentRepository.save(
            ReviewComment(
                reviewId = reviewId,
                authorId = userId(),
                content = request.content,
            ),
        )

        return ReviewResponse.Id(comment.id!!)
    }

    @Transactional
    suspend fun updateComment(commentId: Long, request: UpdateCommentRequest) {
        val comment = reviewCommentRepository.findById(commentId)
            ?: throw BusinessException(ReviewError.CommentNotFound)

        if (comment.authorId != userId()) {
            throw BusinessException(ReviewError.AccessDenied)
        }

        reviewCommentRepository.save(comment.copy(content = request.content))
    }

    @Transactional
    suspend fun deleteComment(commentId: Long) {
        val comment = reviewCommentRepository.findById(commentId)
            ?: throw BusinessException(ReviewError.CommentNotFound)

        if (comment.authorId != userId()) {
            throw BusinessException(ReviewError.AccessDenied)
        }

        reviewCommentRepository.delete(comment)
    }

    private suspend fun buildReviewDetail(review: ProblemReview): ReviewResponse.Detail {
        val comments = reviewCommentRepository.findAllByReviewIdOrderByCreatedAtAsc(review.id!!).toList()
        val userIds = (listOf(review.requesterId) + listOfNotNull(review.reviewerId) + comments.map { it.authorId }).distinct()
        val userMap = userRepository.findAllByIdIn(userIds).toList().associateBy { it.id }

        return ReviewResponse.Detail(
            id = review.id,
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

    private fun kr.solve.domain.user.domain.entity.User.toAuthor() = ReviewResponse.Author(
        id = id!!,
        username = username,
        displayName = displayName,
        profileImage = profileImage,
    )
}
