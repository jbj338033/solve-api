package kr.solve.domain.user.domain.entity

import com.github.f4b6a3.ulid.UlidCreator
import kr.solve.common.entity.BaseEntity
import kr.solve.common.enums.Tier
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Version
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime
import java.util.UUID

@Table("user_tier_histories")
data class UserTierHistory(
    @Id val id: UUID = UlidCreator.getMonotonicUlid().toUuid(),
    @Version val version: Long? = null,
    val userId: UUID,
    val oldTier: Tier,
    val newTier: Tier,
    val rating: Int,
    val achievedAt: LocalDateTime,
) : BaseEntity()
