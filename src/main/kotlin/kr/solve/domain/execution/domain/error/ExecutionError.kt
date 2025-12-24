package kr.solve.domain.execution.domain.error

import kr.solve.global.error.BaseError
import org.springframework.http.HttpStatus

sealed class ExecutionError(
    override val status: HttpStatus,
    override val message: String,
) : BaseError {
    data object ExecutionUnavailable : ExecutionError(HttpStatus.SERVICE_UNAVAILABLE, "실행 서버에 연결할 수 없습니다")
}
