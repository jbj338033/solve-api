package kr.solve.infra.judge

import kr.solve.domain.submission.domain.enums.Language

data class JudgeRequest(
    val submissionId: Long,
    val language: Language,
    val code: String,
    val timeLimit: Int,
    val memoryLimit: Int,
    val testcases: List<TestCase>,
) {
    data class TestCase(
        val id: Long,
        val input: String,
        val output: String,
        val order: Int,
    )
}
