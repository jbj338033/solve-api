package kr.solve.domain.submission.domain.entity

import kr.solve.domain.submission.domain.enums.JudgeResult
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.annotation.Version
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("submission_results")
data class SubmissionResult(
    @Id val id: Long? = null,
    @Version val version: Long? = null,
    @CreatedDate @Column("created_at") val createdAt: LocalDateTime? = null,
    @LastModifiedDate @Column("updated_at") val updatedAt: LocalDateTime? = null,
    val submissionId: Long,
    val testcaseId: Long,
    val result: JudgeResult,
    val timeUsed: Int? = null,
    val memoryUsed: Int? = null,
)
