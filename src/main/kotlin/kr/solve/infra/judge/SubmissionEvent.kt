package kr.solve.infra.judge

import kr.solve.domain.submission.domain.enums.JudgeResult
import kr.solve.domain.submission.domain.enums.Language
import kr.solve.domain.submission.domain.enums.SubmissionStatus
import java.time.LocalDateTime

data class SubmissionEvent(
    val type: String,
    val data: Data,
) {
    data class Data(
        val id: Long?,
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
        val id: Long?,
        val title: String,
    )

    data class Contest(
        val id: Long?,
        val title: String,
    )

    data class User(
        val id: Long?,
        val username: String,
        val displayName: String,
        val profileImage: String,
    )
}
