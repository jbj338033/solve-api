package kr.solve.domain.contest.domain.entity

import com.github.f4b6a3.ulid.UlidCreator
import kr.solve.common.entity.BaseEntity
import kr.solve.domain.contest.domain.enums.ContestType
import kr.solve.domain.contest.domain.enums.ScoreboardType
import kr.solve.domain.contest.domain.enums.ScoringType
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Version
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime
import java.util.UUID

@Table("contests")
data class Contest(
    @Id val id: UUID = UlidCreator.getMonotonicUlid().toUuid(),
    @Version val version: Long? = null,
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
