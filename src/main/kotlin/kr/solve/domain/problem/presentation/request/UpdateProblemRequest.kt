package kr.solve.domain.problem.presentation.request

import jakarta.validation.Valid
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.Size
import kr.solve.domain.problem.domain.enums.ProblemDifficulty
import kr.solve.domain.problem.domain.enums.ProblemType

data class UpdateProblemRequest(
    @field:Size(max = 200)
    val title: String? = null,
    val description: String? = null,
    val inputFormat: String? = null,
    val outputFormat: String? = null,
    val difficulty: ProblemDifficulty? = null,
    @field:Min(100)
    @field:Max(10000)
    val timeLimit: Int? = null,
    @field:Min(16)
    @field:Max(1024)
    val memoryLimit: Int? = null,
    val isPublic: Boolean? = null,
    val type: ProblemType? = null,
    val checkerCode: String? = null,
    val checkerLanguage: String? = null,
    val interactorCode: String? = null,
    val interactorLanguage: String? = null,
    @field:Valid
    val examples: List<ExampleRequest>? = null,
    val tagIds: List<Long>? = null,
)
