package kr.solve.domain.contest.domain.entity

import kr.solve.common.entity.BaseEntity
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.util.UUID

@Table("contest_problems")
data class ContestProblem(
    val contestId: UUID,
    val problemId: UUID,
    @Column("order")
    val order: Int,
    val score: Int? = null,
) : BaseEntity()
