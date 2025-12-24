package kr.solve.domain.user.domain.error

import kr.solve.global.error.BaseError
import org.springframework.http.HttpStatus

sealed class UserError(
    override val status: HttpStatus,
    override val message: String,
) : BaseError {
    data object NotFound : UserError(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다")
    data object OAuthAlreadyLinked : UserError(HttpStatus.CONFLICT, "이미 연동된 계정입니다")
    data object CannotUnlinkLastOAuth : UserError(HttpStatus.BAD_REQUEST, "마지막 OAuth 연동은 해제할 수 없습니다")
}
