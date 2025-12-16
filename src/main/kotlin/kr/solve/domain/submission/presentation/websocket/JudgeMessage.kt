package kr.solve.domain.submission.presentation.websocket

import kr.solve.domain.submission.domain.enums.JudgeResult
import kr.solve.domain.submission.domain.enums.Language

data class JudgeMessage(
    val type: Type,
    val data: Any? = null,
) {
    enum class Type {
        INIT,
        CREATED,
        PROGRESS,
        COMPLETE,
        ERROR,
    }

    data class InitData(
        val token: String? = null,
        val problemId: Long,
        val contestId: Long? = null,
        val language: Language,
        val code: String,
    )

    data class CreatedData(
        val submissionId: Long,
    )

    data class ProgressData(
        val testcaseId: Long,
        val result: JudgeResult,
        val time: Int,
        val memory: Int,
        val score: Int,
        val progress: Int,
    )

    data class CompleteData(
        val result: JudgeResult,
        val score: Int,
        val time: Int,
        val memory: Int,
        val error: String? = null,
    )
}
