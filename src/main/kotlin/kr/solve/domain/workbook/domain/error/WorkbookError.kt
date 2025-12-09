package kr.solve.domain.workbook.domain.error

import kr.solve.global.error.BaseError
import org.springframework.http.HttpStatus

enum class WorkbookError(
    override val status: HttpStatus,
    override val message: String,
) : BaseError {
    NOT_FOUND(HttpStatus.NOT_FOUND, "워크북을 찾을 수 없습니다"),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "워크북에 접근할 수 없습니다"),
    AUTHOR_NOT_FOUND(HttpStatus.NOT_FOUND, "작성자를 찾을 수 없습니다"),
}
