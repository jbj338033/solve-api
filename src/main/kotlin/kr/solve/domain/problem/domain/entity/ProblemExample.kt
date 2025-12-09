package kr.solve.domain.problem.domain.entity

import kr.solve.common.entity.BaseEntity
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.util.UUID

@Table("problem_examples")
data class ProblemExample(
    val problemId: UUID,
    val input: String,
    val output: String,
    @Column("order")
    val order: Int,
) : BaseEntity()
