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
    data object SolutionNotFound : ProblemError(HttpStatus.BAD_REQUEST, "정답 코드가 등록되지 않았습니다")
}
