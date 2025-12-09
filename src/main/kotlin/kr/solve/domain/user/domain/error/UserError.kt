package kr.solve.domain.user.domain.error

import kr.solve.global.error.BaseError
import org.springframework.http.HttpStatus

enum class UserError(
    override val status: HttpStatus,
    override val message: String,
) : BaseError {
    NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다"),
    DUPLICATE_USERNAME(HttpStatus.CONFLICT, "이미 사용중인 사용자명입니다: %s"),
    INVALID_USERNAME(HttpStatus.BAD_REQUEST, "유효하지 않은 사용자명입니다"),
    OAUTH_ALREADY_LINKED(HttpStatus.CONFLICT, "이미 연동된 계정입니다"),
    CANNOT_UNLINK_LAST_OAUTH(HttpStatus.BAD_REQUEST, "마지막 OAuth 연동은 해제할 수 없습니다"),
}
