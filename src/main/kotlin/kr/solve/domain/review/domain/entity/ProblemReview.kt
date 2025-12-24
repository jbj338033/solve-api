package kr.solve.domain.review.domain.entity

import kr.solve.domain.review.domain.enums.ReviewStatus
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.annotation.Version
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("problem_reviews")
data class ProblemReview(
    @Id val id: Long? = null,
    @Version val version: Long? = null,
    @CreatedDate @Column("created_at") val createdAt: LocalDateTime? = null,
    @LastModifiedDate @Column("updated_at") val updatedAt: LocalDateTime? = null,
    val problemId: Long,
    val requesterId: Long,
    val reviewerId: Long? = null,
    val status: ReviewStatus = ReviewStatus.PENDING,
    val summary: String? = null,
    val reviewedAt: LocalDateTime? = null,
)
