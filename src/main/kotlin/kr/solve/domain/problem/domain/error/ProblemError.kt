package kr.solve.domain.problem.domain.error

import kr.solve.global.error.BaseError
import org.springframework.http.HttpStatus

enum class ProblemError(
    override val status: HttpStatus,
    override val message: String,
) : BaseError {
    NOT_FOUND(HttpStatus.NOT_FOUND, "문제를 찾을 수 없습니다"),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "문제에 접근할 수 없습니다"),
    AUTHOR_NOT_FOUND(HttpStatus.NOT_FOUND, "작성자를 찾을 수 없습니다"),
}
