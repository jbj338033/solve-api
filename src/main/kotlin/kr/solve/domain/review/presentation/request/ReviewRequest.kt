package kr.solve.domain.review.presentation.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank

@Schema(name = "CreateReviewRequest", description = "검수 요청")
data class CreateReviewRequest(
    @Schema(description = "검수 요청 메시지", example = "검수 부탁드립니다.")
    val message: String? = null,
)

@Schema(name = "ReviewDecisionRequest", description = "검수 결정")
data class ReviewDecisionRequest(
    @Schema(description = "검수 결과 요약", example = "테스트케이스를 추가해주세요.")
    @field:NotBlank
    val summary: String,
)

@Schema(name = "CreateCommentRequest", description = "코멘트 작성")
data class CreateCommentRequest(
    @Schema(description = "코멘트 내용", example = "입력 형식이 명확하지 않습니다.")
    @field:NotBlank
    val content: String,
)

@Schema(name = "UpdateCommentRequest", description = "코멘트 수정")
data class UpdateCommentRequest(
    @Schema(description = "코멘트 내용", example = "입력 형식이 명확하지 않습니다.")
    @field:NotBlank
    val content: String,
)
