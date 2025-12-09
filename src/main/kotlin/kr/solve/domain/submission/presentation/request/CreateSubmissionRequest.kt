package kr.solve.domain.submission.presentation.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import kr.solve.domain.submission.domain.enums.Language

data class CreateSubmissionRequest(
    val language: Language,
    @field:NotBlank
    @field:Size(max = 65535)
    val code: String,
)
