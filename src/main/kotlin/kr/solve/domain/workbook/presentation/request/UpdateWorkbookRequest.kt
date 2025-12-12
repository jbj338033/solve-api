package kr.solve.domain.workbook.presentation.request

import jakarta.validation.constraints.Size

data class UpdateWorkbookRequest(
    @field:Size(max = 200)
    val title: String? = null,
    val description: String? = null,
    val problemIds: List<Long>? = null,
)
