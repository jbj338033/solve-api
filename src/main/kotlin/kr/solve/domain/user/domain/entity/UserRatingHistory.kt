package kr.solve.domain.user.domain.entity

import kr.solve.domain.user.domain.enums.RatingType
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.annotation.Version
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("user_rating_histories")
data class UserRatingHistory(
    @Id val id: Long? = null,
    @Version val version: Long? = null,
    @CreatedDate @Column("created_at") val createdAt: LocalDateTime? = null,
    @LastModifiedDate @Column("updated_at") val updatedAt: LocalDateTime? = null,
    val userId: Long,
    val contestId: Long?,
    val rating: Int,
    val ratingType: RatingType,
    val recordedAt: LocalDateTime,
)
