package kr.solve.domain.contest.domain.entity

import kr.solve.common.entity.BaseEntity
import kr.solve.domain.contest.domain.enums.ContestType
import kr.solve.domain.contest.domain.enums.ScoreboardType
import kr.solve.domain.contest.domain.enums.ScoringType
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime
import java.util.UUID

@Table("contests")
data class Contest(
    val title: String,
    val description: String? = null,
    val hostId: UUID,
    val startAt: LocalDateTime,
    val endAt: LocalDateTime,
    val type: ContestType = ContestType.PUBLIC,
    val inviteCode: String? = null,
    val scoringType: ScoringType = ScoringType.IOI,
    val scoreboardType: ScoreboardType = ScoreboardType.REALTIME,
    val freezeMinutes: Int? = null,
    val isRated: Boolean = false,
) : BaseEntity()
