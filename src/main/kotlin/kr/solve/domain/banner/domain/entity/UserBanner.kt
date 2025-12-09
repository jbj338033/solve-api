package kr.solve.domain.banner.domain.entity

import kr.solve.common.entity.BaseEntity
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime
import java.util.UUID

@Table("user_banners")
data class UserBanner(
    val userId: UUID,
    val bannerId: UUID,
    val acquiredAt: LocalDateTime = LocalDateTime.now(),
) : BaseEntity()
