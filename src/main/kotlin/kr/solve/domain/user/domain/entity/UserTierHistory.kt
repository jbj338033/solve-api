package kr.solve.domain.user.domain.entity

import kr.solve.common.entity.BaseEntity
import kr.solve.common.enums.Tier
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime
import java.util.UUID

@Table("user_tier_histories")
data class UserTierHistory(
    val userId: UUID,
    val oldTier: Tier,
    val newTier: Tier,
    val rating: Int,
    val achievedAt: LocalDateTime,
) : BaseEntity()
