package kr.solve.global.error

import io.github.oshai.kotlinlogging.KotlinLogging
import kr.solve.common.error.CommonError
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

private val logger = KotlinLogging.logger {}

@RestControllerAdvice
class GlobalExceptionHandler {
    @ExceptionHandler(BusinessException::class)
    fun handleBusinessException(exception: BusinessException): ResponseEntity<ErrorResponse> = ErrorResponse.of(exception)

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleMethodArgumentNotValidException(exception: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {

    }

    @ExceptionHandler(Exception::class)
    fun handleException(exception: Exception): ResponseEntity<ErrorResponse> {
        logger.error(exception) { "Unhandled exception occurred" }
        return ErrorResponse.of(CommonError.INTERNAL_SERVER_ERROR)
    }
}
