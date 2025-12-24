package kr.solve.domain.problem.presentation.request

import jakarta.validation.constraints.NotBlank

data class ProblemSourceRequest(
    @field:NotBlank
    val solutionCode: String,
    @field:NotBlank
    val solutionLanguage: String,
    val generatorCode: String? = null,
    val generatorLanguage: String? = null,
)
