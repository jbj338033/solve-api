package kr.solve.domain.tag.domain.error

import kr.solve.global.error.BaseError
import org.springframework.http.HttpStatus

enum class TagError(
    override val status: HttpStatus,
    override val message: String,
) : BaseError {
    NOT_FOUND(HttpStatus.NOT_FOUND, "태그를 찾을 수 없습니다"),
    DUPLICATE(HttpStatus.CONFLICT, "이미 존재하는 태그입니다: %s"),
}
