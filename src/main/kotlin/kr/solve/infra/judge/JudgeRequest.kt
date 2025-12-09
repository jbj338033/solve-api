package kr.solve.infra.judge

import kr.solve.domain.submission.domain.enums.Language
import java.util.UUID

data class JudgeRequest(
    val submissionId: UUID,
    val language: Language,
    val code: String,
    val timeLimit: Int,
    val memoryLimit: Int,
    val testcases: List<TestCase>,
) {
    data class TestCase(
        val id: UUID,
        val input: String,
        val output: String,
        val order: Int,
    )
}
