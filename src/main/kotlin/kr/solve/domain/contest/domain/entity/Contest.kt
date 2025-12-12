package kr.solve.domain.contest.domain.entity

import kr.solve.domain.contest.domain.enums.ContestType
import kr.solve.domain.contest.domain.enums.ScoreboardType
import kr.solve.domain.contest.domain.enums.ScoringType
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.annotation.Version
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("contests")
data class Contest(
    @Id val id: Long? = null,
    @Version val version: Long? = null,
    @CreatedDate @Column("created_at") val createdAt: LocalDateTime? = null,
    @LastModifiedDate @Column("updated_at") val updatedAt: LocalDateTime? = null,
    val title: String,
    val description: String? = null,
    val hostId: Long,
    val startAt: LocalDateTime,
    val endAt: LocalDateTime,
    val type: ContestType = ContestType.PUBLIC,
    val inviteCode: String? = null,
    val scoringType: ScoringType = ScoringType.IOI,
    val scoreboardType: ScoreboardType = ScoreboardType.REALTIME,
    val freezeMinutes: Int? = null,
    val isRated: Boolean = false,
)
