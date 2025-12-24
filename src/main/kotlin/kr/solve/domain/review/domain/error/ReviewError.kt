package kr.solve.domain.review.domain.error

import kr.solve.global.error.BaseError
import org.springframework.http.HttpStatus

sealed class ReviewError(
    override val status: HttpStatus,
    override val message: String,
) : BaseError {
    data object NotFound : ReviewError(HttpStatus.NOT_FOUND, "리뷰를 찾을 수 없습니다")
    data object AlreadyReviewed : ReviewError(HttpStatus.CONFLICT, "이미 처리된 리뷰입니다")
    data object CannotRequestReview : ReviewError(HttpStatus.BAD_REQUEST, "검수를 요청할 수 없는 상태입니다")
    data object CommentNotFound : ReviewError(HttpStatus.NOT_FOUND, "코멘트를 찾을 수 없습니다")
    data object AccessDenied : ReviewError(HttpStatus.FORBIDDEN, "접근 권한이 없습니다")
}
