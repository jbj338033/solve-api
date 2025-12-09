package kr.solve.domain.user.domain.entity

import kr.solve.common.entity.BaseEntity
import kr.solve.domain.user.domain.enums.RatingType
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime
import java.util.UUID

@Table("user_rating_histories")
data class UserRatingHistory(
    val userId: UUID,
    val contestId: UUID?,
    val rating: Int,
    val ratingType: RatingType,
    val recordedAt: LocalDateTime,
) : BaseEntity()
