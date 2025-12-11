package kr.solve.domain.user.domain.entity

import com.github.f4b6a3.ulid.UlidCreator
import kr.solve.common.entity.BaseEntity
import kr.solve.domain.user.domain.enums.RatingType
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Version
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime
import java.util.UUID

@Table("user_rating_histories")
data class UserRatingHistory(
    @Id val id: UUID = UlidCreator.getMonotonicUlid().toUuid(),
    @Version val version: Long? = null,
    val userId: UUID,
    val contestId: UUID?,
    val rating: Int,
    val ratingType: RatingType,
    val recordedAt: LocalDateTime,
) : BaseEntity()
