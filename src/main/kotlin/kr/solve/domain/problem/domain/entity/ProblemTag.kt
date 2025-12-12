package kr.solve.domain.problem.domain.entity

import org.springframework.data.relational.core.mapping.Table

@Table("problem_tags")
data class ProblemTag(
    val problemId: Long,
    val tagId: Long,
)
