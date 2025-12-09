package kr.solve.domain.problem.domain.entity

import kr.solve.common.entity.BaseEntity
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.util.UUID

@Table("problem_subtasks")
data class ProblemSubtask(
    val problemId: UUID,
    val score: Int,
    @Column("order")
    val order: Int,
    val description: String? = null,
) : BaseEntity()
