package kr.solve.domain.submission.domain.entity

import kr.solve.domain.submission.domain.enums.JudgeResult
import kr.solve.domain.submission.domain.enums.Language
import kr.solve.domain.submission.domain.enums.SubmissionStatus
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.annotation.Version
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("submissions")
data class Submission(
    @Id val id: Long? = null,
    @Version val version: Long? = null,
    @CreatedDate @Column("created_at") val createdAt: LocalDateTime? = null,
    @LastModifiedDate @Column("updated_at") val updatedAt: LocalDateTime? = null,
    val problemId: Long,
    val userId: Long,
    val contestId: Long? = null,
    val language: Language,
    val code: String,
    val status: SubmissionStatus = SubmissionStatus.PENDING,
    val result: JudgeResult? = null,
    val score: Int? = null,
    val timeUsed: Int? = null,
    val memoryUsed: Int? = null,
    val error: String? = null,
    val judgedAt: LocalDateTime? = null,
)
