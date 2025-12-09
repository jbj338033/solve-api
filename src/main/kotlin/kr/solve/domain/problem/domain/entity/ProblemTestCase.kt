package kr.solve.domain.problem.domain.entity

import kr.solve.common.entity.BaseEntity
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.util.UUID

@Table("problem_test_cases")
data class ProblemTestCase(
    val problemId: UUID,
    val input: String,
    val output: String,
    @Column("order")
    val order: Int,
    val subtaskId: UUID? = null,
) : BaseEntity()
