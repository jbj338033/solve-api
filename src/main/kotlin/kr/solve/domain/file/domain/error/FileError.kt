package kr.solve.domain.file.domain.error

import kr.solve.global.error.BaseError
import org.springframework.http.HttpStatus

enum class FileError(
    override val status: HttpStatus,
    override val message: String,
) : BaseError {
    INVALID_CONTENT_TYPE(HttpStatus.BAD_REQUEST, "허용되지 않는 파일 형식입니다"),
    FILE_TOO_LARGE(HttpStatus.BAD_REQUEST, "파일 크기가 너무 큽니다"),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "파일 업로드 권한이 없습니다"),
}
