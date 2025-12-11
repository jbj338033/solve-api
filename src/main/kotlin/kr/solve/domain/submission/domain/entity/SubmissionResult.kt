package kr.solve.domain.submission.domain.entity

import com.github.f4b6a3.ulid.UlidCreator
import kr.solve.common.entity.BaseEntity
import kr.solve.domain.submission.domain.enums.JudgeResult
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Version
import org.springframework.data.relational.core.mapping.Table
import java.util.UUID

@Table("submission_results")
data class SubmissionResult(
    @Id val id: UUID = UlidCreator.getMonotonicUlid().toUuid(),
    @Version val version: Long? = null,
    val submissionId: UUID,
    val testcaseId: UUID,
    val result: JudgeResult,
    val timeUsed: Int? = null,
    val memoryUsed: Int? = null,
) : BaseEntity()
