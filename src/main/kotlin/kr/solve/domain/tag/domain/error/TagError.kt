package kr.solve.domain.tag.domain.error

import kr.solve.global.error.BaseError
import org.springframework.http.HttpStatus

sealed class TagError(
    override val status: HttpStatus,
    override val message: String,
) : BaseError {
    data object NotFound : TagError(HttpStatus.NOT_FOUND, "태그를 찾을 수 없습니다")
    data class Duplicate(val name: String) : TagError(HttpStatus.CONFLICT, "이미 존재하는 태그입니다: $name")
}
