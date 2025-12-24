package kr.solve.domain.problem.presentation.response

data class ProblemSourceResponse(
    val solutionCode: String,
    val solutionLanguage: String,
    val generatorCode: String?,
    val generatorLanguage: String?,
)
