package kr.solve.domain.review.presentation.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import kr.solve.domain.review.application.service.AdminReviewService
import kr.solve.domain.review.presentation.request.CreateCommentRequest
import kr.solve.domain.review.presentation.request.ReviewDecisionRequest
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Admin - Review", description = "검수 관리 API")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
@RestController
@RequestMapping("/admin/reviews")
class AdminReviewController(
    private val adminReviewService: AdminReviewService,
) {
    @Operation(summary = "검수 대기 목록 조회")
    @GetMapping
    suspend fun getPendingReviews(
        @RequestParam(required = false) cursor: Long?,
        @RequestParam(defaultValue = "20") limit: Int,
    ) = adminReviewService.getPendingReviews(cursor, limit.coerceIn(1, 100))

    @Operation(summary = "검수 상세 조회")
    @GetMapping("/{reviewId}")
    suspend fun getReview(
        @PathVariable reviewId: Long,
    ) = adminReviewService.getReview(reviewId)

    @Operation(summary = "검수 승인")
    @PostMapping("/{reviewId}/approve")
    suspend fun approve(
        @PathVariable reviewId: Long,
        @Valid @RequestBody request: ReviewDecisionRequest,
    ) = adminReviewService.approve(reviewId, request)

    @Operation(summary = "변경 요청")
    @PostMapping("/{reviewId}/request-changes")
    suspend fun requestChanges(
        @PathVariable reviewId: Long,
        @Valid @RequestBody request: ReviewDecisionRequest,
    ) = adminReviewService.requestChanges(reviewId, request)

    @Operation(summary = "검수 코멘트 작성")
    @PostMapping("/{reviewId}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    suspend fun createComment(
        @PathVariable reviewId: Long,
        @Valid @RequestBody request: CreateCommentRequest,
    ) = adminReviewService.createComment(reviewId, request)
}
