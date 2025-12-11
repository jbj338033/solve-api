package kr.solve.domain.contest.domain.entity

import com.github.f4b6a3.ulid.UlidCreator
import kr.solve.common.entity.BaseEntity
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Version
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime
import java.util.UUID

@Table("contest_participants")
data class ContestParticipant(
    @Id val id: UUID = UlidCreator.getMonotonicUlid().toUuid(),
    @Version val version: Long? = null,
    val contestId: UUID,
    val userId: UUID,
    val totalScore: Int = 0,
    val penalty: Long = 0,
    @Column("rank") val rank: Int? = null,
    val ratingChange: Int? = null,
    val joinedAt: LocalDateTime = LocalDateTime.now(),
) : BaseEntity()
