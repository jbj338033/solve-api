package kr.solve.domain.contest.domain.entity

import kr.solve.common.entity.BaseEntity
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime
import java.util.UUID

@Table("contest_participants")
data class ContestParticipant(
    val contestId: UUID,
    val userId: UUID,
    val totalScore: Int = 0,
    val penalty: Long = 0,
    @Column("rank")
    val rank: Int? = null,
    val ratingChange: Int? = null,
    val joinedAt: LocalDateTime = LocalDateTime.now(),
) : BaseEntity()
