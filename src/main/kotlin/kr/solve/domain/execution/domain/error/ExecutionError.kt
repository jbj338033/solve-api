package kr.solve.domain.execution.domain.error

import kr.solve.global.error.BaseError
import org.springframework.http.HttpStatus

enum class ExecutionError(
    override val status: HttpStatus,
    override val message: String,
) : BaseError {
    EXECUTION_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "실행 서버에 연결할 수 없습니다"),
}
