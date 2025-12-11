package kr.solve.domain.contest.presentation.request

import jakarta.validation.Valid
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.Size
import kr.solve.domain.contest.domain.enums.ContestType
import kr.solve.domain.contest.domain.enums.ScoreboardType
import kr.solve.domain.contest.domain.enums.ScoringType
import java.time.LocalDateTime

data class AdminUpdateContestRequest(
    @field:Size(max = 200)
    val title: String? = null,
    val description: String? = null,
    val startAt: LocalDateTime? = null,
    val endAt: LocalDateTime? = null,
    val type: ContestType? = null,
    val scoringType: ScoringType? = null,
    val scoreboardType: ScoreboardType? = null,
    @field:Positive
    val freezeMinutes: Int? = null,
    @field:Valid
    val problems: List<AdminContestProblemRequest>? = null,
)
