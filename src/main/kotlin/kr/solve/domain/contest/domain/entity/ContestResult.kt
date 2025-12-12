package kr.solve.domain.contest.domain.entity

import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.annotation.Version
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("contest_results")
data class ContestResult(
    @Id val id: Long? = null,
    @Version val version: Long? = null,
    @CreatedDate @Column("created_at") val createdAt: LocalDateTime? = null,
    @LastModifiedDate @Column("updated_at") val updatedAt: LocalDateTime? = null,
    val contestId: Long,
    val userId: Long,
    val problemId: Long,
    val score: Int = 0,
    val attempts: Int = 0,
    val solvedAt: LocalDateTime? = null,
)
