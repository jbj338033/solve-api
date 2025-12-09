package kr.solve.domain.submission.domain.entity

import kr.solve.common.entity.BaseEntity
import kr.solve.domain.submission.domain.enums.JudgeResult
import org.springframework.data.relational.core.mapping.Table
import java.util.UUID

@Table("submission_results")
data class SubmissionResult(
    val submissionId: UUID,
    val testcaseId: UUID,
    val result: JudgeResult,
    val timeUsed: Int? = null,
    val memoryUsed: Int? = null,
) : BaseEntity()
