package kr.solve.infra.judge

import kr.solve.domain.submission.domain.enums.JudgeResult
import kr.solve.domain.submission.domain.enums.Language
import kr.solve.domain.submission.domain.enums.SubmissionStatus
import java.time.LocalDateTime
import java.util.UUID

data class SubmissionEvent(
    val type: String,
    val data: Data,
) {
    data class Data(
        val id: UUID,
        val problem: Problem,
        val contest: Contest?,
        val user: User,
        val language: Language,
        val status: SubmissionStatus,
        val result: JudgeResult?,
        val score: Int?,
        val time: Int?,
        val memory: Int?,
        val createdAt: LocalDateTime?,
    )

    data class Problem(
        val id: UUID,
        val title: String,
    )

    data class Contest(
        val id: UUID,
        val title: String,
    )

    data class User(
        val id: UUID,
        val username: String,
        val displayName: String,
        val profileImage: String,
    )
}
