package kr.solve.global.error

import org.springframework.http.ResponseEntity
import java.time.LocalDateTime

data class ErrorResponse(
    val code: String,
    val status: Int,
    val message: String,
    val timestamp: String = LocalDateTime.now().toString(),
) {
    companion object {
        fun of(error: BaseError): ResponseEntity<ErrorResponse> {
            val response =
                ErrorResponse(
                    code = (error as Enum<*>).name,
                    status = error.status.value(),
                    message = error.message,
                )
            return ResponseEntity.status(error.status).body(response)
        }

        fun of(exception: BusinessException): ResponseEntity<ErrorResponse> {
            val error = exception.error
            val response =
                ErrorResponse(
                    code = (error as Enum<*>).name,
                    status = error.status.value(),
                    message = exception.message ?: error.message,
                )
            return ResponseEntity.status(error.status).body(response)
        }
    }
}
