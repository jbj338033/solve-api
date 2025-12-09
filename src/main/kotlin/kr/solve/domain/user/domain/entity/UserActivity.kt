package kr.solve.domain.user.domain.entity

import kr.solve.common.entity.BaseEntity
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDate
import java.util.UUID

@Table("user_activities")
data class UserActivity(
    val userId: UUID,
    val date: LocalDate,
    val solvedCount: Int = 0,
    val submissionCount: Int = 0,
) : BaseEntity()
