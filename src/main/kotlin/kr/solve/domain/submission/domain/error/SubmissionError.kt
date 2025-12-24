package kr.solve.domain.submission.domain.error

import kr.solve.global.error.BaseError
import org.springframework.http.HttpStatus

sealed class SubmissionError(
    override val status: HttpStatus,
    override val message: String,
) : BaseError {
    data object NotFound : SubmissionError(HttpStatus.NOT_FOUND, "제출을 찾을 수 없습니다")
}
