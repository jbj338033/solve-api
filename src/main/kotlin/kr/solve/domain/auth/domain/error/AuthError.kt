package kr.solve.domain.auth.domain.error

import kr.solve.global.error.BaseError
import org.springframework.http.HttpStatus

sealed class AuthError(
    override val status: HttpStatus,
    override val message: String,
) : BaseError {
    data object InvalidToken : AuthError(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다")
    data object ExpiredToken : AuthError(HttpStatus.UNAUTHORIZED, "만료된 토큰입니다")
    data class OAuthFailed(val reason: String) : AuthError(HttpStatus.BAD_REQUEST, "OAuth 인증에 실패했습니다: $reason")
    data object InvalidRefreshToken : AuthError(HttpStatus.UNAUTHORIZED, "유효하지 않은 리프레시 토큰입니다")
    data object UnsupportedProvider : AuthError(HttpStatus.BAD_REQUEST, "지원하지 않는 OAuth 제공자입니다")
}
