package kr.solve.domain.review.presentation.response

import io.swagger.v3.oas.annotations.media.Schema
import kr.solve.domain.review.domain.enums.ReviewStatus
import java.time.LocalDateTime

object ReviewResponse {
    @Schema(name = "Review.Author", description = "작성자 정보")
    data class Author(
        val id: Long,
        val username: String,
        val displayName: String,
        val profileImage: String,
    )

    @Schema(name = "Review.Summary", description = "리뷰 요약")
    data class Summary(
        val id: Long,
        val problemId: Long,
        val requester: Author,
        val reviewer: Author?,
        val status: ReviewStatus,
        val createdAt: LocalDateTime?,
        val reviewedAt: LocalDateTime?,
    )

    @Schema(name = "Review.Detail", description = "리뷰 상세")
    data class Detail(
        val id: Long,
        val problemId: Long,
        val requester: Author,
        val reviewer: Author?,
        val status: ReviewStatus,
        val summary: String?,
        val createdAt: LocalDateTime?,
        val reviewedAt: LocalDateTime?,
        val comments: List<Comment>,
    )

    @Schema(name = "Review.Comment", description = "리뷰 코멘트")
    data class Comment(
        val id: Long,
        val author: Author,
        val content: String,
        val createdAt: LocalDateTime?,
        val updatedAt: LocalDateTime?,
    )
}
