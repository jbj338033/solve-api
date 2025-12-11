package kr.solve.domain.problem.presentation.request

import jakarta.validation.Valid
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import kr.solve.domain.problem.domain.enums.ProblemType
import java.util.UUID

data class AdminCreateProblemRequest(
    @field:NotBlank
    @field:Size(max = 200)
    val title: String,
    @field:NotBlank
    val description: String,
    @field:NotBlank
    val inputFormat: String,
    @field:NotBlank
    val outputFormat: String,
    @field:Min(1)
    @field:Max(30)
    val difficulty: Int,
    @field:Min(100)
    @field:Max(10000)
    val timeLimit: Int = 1000,
    @field:Min(16)
    @field:Max(1024)
    val memoryLimit: Int = 256,
    val isPublic: Boolean = false,
    val type: ProblemType = ProblemType.STANDARD,
    val checkerCode: String? = null,
    val checkerLanguage: String? = null,
    val interactorCode: String? = null,
    val interactorLanguage: String? = null,
    @field:Valid
    val examples: List<AdminExampleRequest> = emptyList(),
    val tagIds: List<UUID> = emptyList(),
)

data class AdminExampleRequest(
    @field:NotBlank
    val input: String,
    @field:NotBlank
    val output: String,
)
