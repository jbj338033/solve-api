package kr.solve.domain.submission.domain.entity

import com.github.f4b6a3.ulid.UlidCreator
import kr.solve.domain.submission.domain.enums.JudgeResult
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.annotation.Version
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime
import java.util.UUID

@Table("submission_results")
data class SubmissionResult(
    @Id val id: UUID = UlidCreator.getMonotonicUlid().toUuid(),
    @Version val version: Long? = null,
    @CreatedDate @Column("created_at") val createdAt: LocalDateTime? = null,
    @LastModifiedDate @Column("updated_at") val updatedAt: LocalDateTime? = null,
    val submissionId: UUID,
    val testcaseId: UUID,
    val result: JudgeResult,
    val timeUsed: Int? = null,
    val memoryUsed: Int? = null,
)
