package kr.solve.infra.judge

import kr.solve.domain.submission.domain.enums.JudgeResult
import java.util.UUID

data class JudgeResponse(
    val result: JudgeResult,
    val score: Int,
    val time: Int,
    val memory: Int,
    val error: String?,
    val testcaseResults: List<TestCaseResult>,
) {
    data class TestCaseResult(
        val testcaseId: UUID,
        val result: JudgeResult,
        val time: Int,
        val memory: Int,
    )
}
