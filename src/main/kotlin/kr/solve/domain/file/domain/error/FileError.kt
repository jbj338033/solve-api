package kr.solve.domain.file.domain.error

import kr.solve.global.error.BaseError
import org.springframework.http.HttpStatus

sealed class FileError(
    override val status: HttpStatus,
    override val message: String,
) : BaseError {
    data object InvalidContentType : FileError(HttpStatus.BAD_REQUEST, "허용되지 않는 파일 형식입니다")
    data object FileTooLarge : FileError(HttpStatus.BAD_REQUEST, "파일 크기가 너무 큽니다")
}
