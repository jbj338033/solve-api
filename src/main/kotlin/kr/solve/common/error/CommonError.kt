package kr.solve.common.error

import kr.solve.global.error.BaseError
import org.springframework.http.HttpStatus

enum class CommonError(
    override val status: HttpStatus,
    override val message: String,
) : BaseError {
    INVALID_REQUEST(
        HttpStatus.BAD_REQUEST,
        "잘못된 요청입니다.",
    ),
    INTERNAL_SERVER_ERROR(
        HttpStatus.INTERNAL_SERVER_ERROR,
        "서버에서 알 수 없는 오류가 발생했습니다.",
    ),
}
