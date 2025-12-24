package kr.solve.domain.banner.domain.error

import kr.solve.global.error.BaseError
import org.springframework.http.HttpStatus

sealed class BannerError(
    override val status: HttpStatus,
    override val message: String,
) : BaseError {
    data object NotFound : BannerError(HttpStatus.NOT_FOUND, "배너를 찾을 수 없습니다")
    data object NotAcquired : BannerError(HttpStatus.BAD_REQUEST, "획득하지 않은 배너입니다")
}
