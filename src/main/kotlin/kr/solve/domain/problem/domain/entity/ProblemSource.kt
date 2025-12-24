package kr.solve.domain.problem.domain.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("problem_sources")
data class ProblemSource(
    @Id val problemId: Long,
    val solutionCode: String,
    val solutionLanguage: String,
    val generatorCode: String? = null,
    val generatorLanguage: String? = null,
)
