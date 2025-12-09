package kr.solve.domain.banner.domain.error

import kr.solve.global.error.BaseError
import org.springframework.http.HttpStatus

enum class BannerError(
    override val status: HttpStatus,
    override val message: String,
) : BaseError {
    NOT_FOUND(HttpStatus.NOT_FOUND, "배너를 찾을 수 없습니다"),
    NOT_ACQUIRED(HttpStatus.BAD_REQUEST, "획득하지 않은 배너입니다"),
}
