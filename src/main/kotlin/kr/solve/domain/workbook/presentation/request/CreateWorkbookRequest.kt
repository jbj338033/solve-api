package kr.solve.domain.workbook.presentation.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class CreateWorkbookRequest(
    @field:NotBlank
    @field:Size(max = 200)
    val title: String,
    val description: String? = null,
    val problemIds: List<Long> = emptyList(),
)
