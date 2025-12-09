package kr.solve.domain.auth.domain.error

import kr.solve.global.error.BaseError
import org.springframework.http.HttpStatus

enum class AuthError(
    override val status: HttpStatus,
    override val message: String,
) : BaseError {
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다"),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "만료된 토큰입니다"),
    OAUTH_FAILED(HttpStatus.BAD_REQUEST, "OAuth 인증에 실패했습니다: %s"),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 리프레시 토큰입니다"),
    UNSUPPORTED_PROVIDER(HttpStatus.BAD_REQUEST, "지원하지 않는 OAuth 제공자입니다"),
}
