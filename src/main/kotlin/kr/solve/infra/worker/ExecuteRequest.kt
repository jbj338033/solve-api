package kr.solve.infra.worker

import kr.solve.domain.submission.domain.enums.Language
import java.util.UUID

data class ExecuteRequest(
    val executionId: UUID,
    val language: Language,
    val code: String,
    val timeLimit: Int,
    val memoryLimit: Int,
)
