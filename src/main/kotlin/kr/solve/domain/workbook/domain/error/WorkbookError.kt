package kr.solve.domain.workbook.domain.error

import kr.solve.global.error.BaseError
import org.springframework.http.HttpStatus

sealed class WorkbookError(
    override val status: HttpStatus,
    override val message: String,
) : BaseError {
    data object NotFound : WorkbookError(HttpStatus.NOT_FOUND, "워크북을 찾을 수 없습니다")
    data object AccessDenied : WorkbookError(HttpStatus.FORBIDDEN, "워크북에 접근할 수 없습니다")
}
