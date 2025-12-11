package kr.solve.domain.tag.presentation.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class AdminCreateTagRequest(
    @field:NotBlank
    @field:Size(max = 50)
    val name: String,
)
