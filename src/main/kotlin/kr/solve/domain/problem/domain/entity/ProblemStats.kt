package kr.solve.domain.problem.domain.entity

import kr.solve.common.entity.BaseEntity
import org.springframework.data.relational.core.mapping.Table
import java.util.UUID

@Table("problem_stats")
data class ProblemStats(
    val problemId: UUID,
    val submissionCount: Int = 0,
    val acceptedCount: Int = 0,
    val userCount: Int = 0,
    val acceptedUserCount: Int = 0,
) : BaseEntity()
