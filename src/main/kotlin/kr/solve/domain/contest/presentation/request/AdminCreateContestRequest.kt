package kr.solve.domain.contest.presentation.request

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.Size
import kr.solve.domain.contest.domain.enums.ContestType
import kr.solve.domain.contest.domain.enums.ScoreboardType
import kr.solve.domain.contest.domain.enums.ScoringType
import java.time.LocalDateTime
import java.util.UUID

data class AdminCreateContestRequest(
    @field:NotBlank
    @field:Size(max = 200)
    val title: String,
    val description: String? = null,
    val startAt: LocalDateTime,
    val endAt: LocalDateTime,
    val type: ContestType = ContestType.PUBLIC,
    val scoringType: ScoringType = ScoringType.IOI,
    val scoreboardType: ScoreboardType = ScoreboardType.REALTIME,
    @field:Positive
    val freezeMinutes: Int? = null,
    val isRated: Boolean = false,
    @field:Valid
    val problems: List<AdminContestProblemRequest> = emptyList(),
)

data class AdminContestProblemRequest(
    val problemId: UUID,
    @field:Positive
    val score: Int? = null,
)
