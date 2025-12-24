package kr.solve.domain.problem.presentation.request

import jakarta.validation.constraints.NotBlank

data class RejectProblemRequest(
    @field:NotBlank
    val reason: String,
)
