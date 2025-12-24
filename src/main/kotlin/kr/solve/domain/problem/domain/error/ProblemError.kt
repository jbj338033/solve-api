package kr.solve.domain.problem.domain.error

import kr.solve.global.error.BaseError
import org.springframework.http.HttpStatus

sealed class ProblemError(
    override val status: HttpStatus,
    override val message: String,
) : BaseError {
    data object NotFound : ProblemError(HttpStatus.NOT_FOUND, "문제를 찾을 수 없습니다")
    data object AccessDenied : ProblemError(HttpStatus.FORBIDDEN, "문제에 접근할 수 없습니다")
    data object CannotEdit : ProblemError(HttpStatus.BAD_REQUEST, "수정할 수 없는 상태입니다")
    data object CannotDelete : ProblemError(HttpStatus.BAD_REQUEST, "삭제할 수 없는 상태입니다")
    data object CannotSubmit : ProblemError(HttpStatus.BAD_REQUEST, "검수 요청할 수 없는 상태입니다")
    data object InsufficientTestCases : ProblemError(HttpStatus.BAD_REQUEST, "테스트케이스가 2개 이상 필요합니다")
    data object CannotApprove : ProblemError(HttpStatus.BAD_REQUEST, "승인할 수 없는 상태입니다")
    data object CannotReject : ProblemError(HttpStatus.BAD_REQUEST, "반려할 수 없는 상태입니다")
}
