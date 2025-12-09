package kr.solve.domain.contest.domain.entity

import kr.solve.common.entity.BaseEntity
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime
import java.util.UUID

@Table("contest_results")
data class ContestResult(
    val contestId: UUID,
    val userId: UUID,
    val problemId: UUID,
    val score: Int = 0,
    val attempts: Int = 0,
    val solvedAt: LocalDateTime? = null,
) : BaseEntity()
