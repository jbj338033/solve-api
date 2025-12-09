package kr.solve.domain.problem.domain.entity

import org.springframework.data.relational.core.mapping.Table
import java.util.UUID

@Table("problem_tags")
data class ProblemTag(
    val problemId: UUID,
    val tagId: UUID,
)
