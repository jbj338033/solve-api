package kr.solve.common.error

import kr.solve.global.error.BaseError
import org.springframework.http.HttpStatus

sealed class CommonError(
    override val status: HttpStatus,
    override val message: String,
) : BaseError {
    data object InvalidRequest : CommonError(HttpStatus.BAD_REQUEST, "잘못된 요청입니다.")
    data object InternalServerError : CommonError(HttpStatus.INTERNAL_SERVER_ERROR, "서버에서 알 수 없는 오류가 발생했습니다.")
}
