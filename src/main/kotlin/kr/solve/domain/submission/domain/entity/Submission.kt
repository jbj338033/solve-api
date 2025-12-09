package kr.solve.domain.submission.domain.entity

import kr.solve.common.entity.BaseEntity
import kr.solve.domain.submission.domain.enums.JudgeResult
import kr.solve.domain.submission.domain.enums.Language
import kr.solve.domain.submission.domain.enums.SubmissionStatus
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime
import java.util.UUID

@Table("submissions")
data class Submission(
    val problemId: UUID,
    val userId: UUID,
    val contestId: UUID? = null,
    val language: Language,
    val code: String,
    val status: SubmissionStatus = SubmissionStatus.PENDING,
    val result: JudgeResult? = null,
    val score: Int? = null,
    val timeUsed: Int? = null,
    val memoryUsed: Int? = null,
    val error: String? = null,
    val judgedAt: LocalDateTime? = null,
) : BaseEntity()
