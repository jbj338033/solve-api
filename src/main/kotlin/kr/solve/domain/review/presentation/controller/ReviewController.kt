package kr.solve.domain.review.presentation.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import kr.solve.domain.review.application.service.ReviewService
import kr.solve.domain.review.presentation.request.CreateCommentRequest
import kr.solve.domain.review.presentation.request.CreateReviewRequest
import kr.solve.domain.review.presentation.request.UpdateCommentRequest
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Review", description = "검수 API")
@SecurityRequirement(name = "bearerAuth")
@RestController
class ReviewController(
    private val reviewService: ReviewService,
) {
    @Operation(summary = "검수 요청")
    @PostMapping("/problems/{problemId}/reviews")
    @ResponseStatus(HttpStatus.CREATED)
    suspend fun createReview(
        @PathVariable problemId: Long,
        @Valid @RequestBody request: CreateReviewRequest,
    ) = reviewService.createReview(problemId, request)

    @Operation(summary = "문제의 검수 목록 조회")
    @GetMapping("/problems/{problemId}/reviews")
    suspend fun getReviews(
        @PathVariable problemId: Long,
    ) = reviewService.getReviewsByProblemId(problemId)

    @Operation(summary = "검수 상세 조회")
    @GetMapping("/reviews/{reviewId}")
    suspend fun getReview(
        @PathVariable reviewId: Long,
    ) = reviewService.getReview(reviewId)

    @Operation(summary = "검수 코멘트 목록 조회")
    @GetMapping("/reviews/{reviewId}/comments")
    suspend fun getComments(
        @PathVariable reviewId: Long,
    ) = reviewService.getComments(reviewId)

    @Operation(summary = "검수 코멘트 작성")
    @PostMapping("/reviews/{reviewId}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    suspend fun createComment(
        @PathVariable reviewId: Long,
        @Valid @RequestBody request: CreateCommentRequest,
    ) = reviewService.createComment(reviewId, request)

    @Operation(summary = "코멘트 수정")
    @PatchMapping("/comments/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    suspend fun updateComment(
        @PathVariable commentId: Long,
        @Valid @RequestBody request: UpdateCommentRequest,
    ) = reviewService.updateComment(commentId, request)

    @Operation(summary = "코멘트 삭제")
    @DeleteMapping("/comments/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    suspend fun deleteComment(
        @PathVariable commentId: Long,
    ) = reviewService.deleteComment(commentId)
}
