package kr.solve.domain.submission.domain.error

import kr.solve.global.error.BaseError
import org.springframework.http.HttpStatus

enum class SubmissionError(
    override val status: HttpStatus,
    override val message: String,
) : BaseError {
    NOT_FOUND(HttpStatus.NOT_FOUND, "제출을 찾을 수 없습니다"),
    PROBLEM_NOT_FOUND(HttpStatus.NOT_FOUND, "문제를 찾을 수 없습니다"),
    PROBLEM_ACCESS_DENIED(HttpStatus.FORBIDDEN, "문제에 접근할 수 없습니다"),
    CONTEST_NOT_STARTED(HttpStatus.BAD_REQUEST, "대회가 시작되지 않았습니다"),
    CONTEST_ENDED(HttpStatus.BAD_REQUEST, "대회가 종료되었습니다"),
    NOT_PARTICIPATING(HttpStatus.FORBIDDEN, "대회에 참가하지 않았습니다"),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다"),
    JUDGE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "채점 서버에 연결할 수 없습니다"),
}
