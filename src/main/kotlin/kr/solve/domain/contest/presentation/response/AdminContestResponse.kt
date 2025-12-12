package kr.solve.domain.contest.presentation.response

import io.swagger.v3.oas.annotations.media.Schema
import kr.solve.domain.contest.domain.entity.Contest
import kr.solve.domain.contest.domain.entity.ContestProblem
import kr.solve.domain.contest.domain.enums.ContestType
import kr.solve.domain.contest.domain.enums.ScoreboardType
import kr.solve.domain.contest.domain.enums.ScoringType
import kr.solve.domain.problem.domain.entity.Problem
import kr.solve.domain.problem.domain.enums.ProblemDifficulty
import java.time.LocalDateTime

fun Contest.toAdminSummary() =
    AdminContestResponse.Summary(
        id = id!!,
        title = title,
        description = description,
        hostId = hostId,
        startAt = startAt,
        endAt = endAt,
        type = type,
        scoringType = scoringType,
        scoreboardType = scoreboardType,
        isRated = isRated,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

fun Contest.toAdminDetail(
    contestProblems: List<ContestProblem>,
    problemMap: Map<Long, Problem>,
    participantCount: Int,
) = AdminContestResponse.Detail(
    id = id!!,
    title = title,
    description = description,
    hostId = hostId,
    startAt = startAt,
    endAt = endAt,
    type = type,
    inviteCode = inviteCode,
    scoringType = scoringType,
    scoreboardType = scoreboardType,
    freezeMinutes = freezeMinutes,
    isRated = isRated,
    problems =
        contestProblems.mapNotNull { cp ->
            problemMap[cp.problemId]?.let { problem ->
                AdminContestResponse.Problem(
                    order = cp.order,
                    score = cp.score,
                    id = problem.id!!,
                    title = problem.title,
                    difficulty = problem.difficulty,
                )
            }
        },
    participantCount = participantCount,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

object AdminContestResponse {
    @Schema(name = "Admin.Contest.Problem", description = "대회 문제 정보")
    data class Problem(
        @Schema(description = "문제 순서 (A=0, B=1, ...)", example = "0")
        val order: Int,
        @Schema(description = "문제 배점 (IOI 방식)", example = "100")
        val score: Int?,
        @Schema(description = "문제 ID")
        val id: Long,
        @Schema(description = "문제 제목", example = "A+B")
        val title: String,
        @Schema(description = "문제 난이도")
        val difficulty: ProblemDifficulty,
    )

    @Schema(name = "Admin.Contest.Summary", description = "대회 요약 정보")
    data class Summary(
        @Schema(description = "대회 ID")
        val id: Long,
        @Schema(description = "대회 제목", example = "2024 신입생 프로그래밍 대회")
        val title: String,
        @Schema(description = "대회 설명")
        val description: String?,
        @Schema(description = "주최자 ID")
        val hostId: Long,
        @Schema(description = "대회 시작 시간")
        val startAt: LocalDateTime,
        @Schema(description = "대회 종료 시간")
        val endAt: LocalDateTime,
        @Schema(description = "대회 유형")
        val type: ContestType,
        @Schema(description = "채점 방식")
        val scoringType: ScoringType,
        @Schema(description = "스코어보드 유형")
        val scoreboardType: ScoreboardType,
        @Schema(description = "레이팅 반영 여부")
        val isRated: Boolean,
        @Schema(description = "생성일시")
        val createdAt: LocalDateTime?,
        @Schema(description = "수정일시")
        val updatedAt: LocalDateTime?,
    )

    @Schema(name = "Admin.Contest.Detail", description = "대회 상세 정보")
    data class Detail(
        @Schema(description = "대회 ID")
        val id: Long,
        @Schema(description = "대회 제목", example = "2024 신입생 프로그래밍 대회")
        val title: String,
        @Schema(description = "대회 설명")
        val description: String?,
        @Schema(description = "주최자 ID")
        val hostId: Long,
        @Schema(description = "대회 시작 시간")
        val startAt: LocalDateTime,
        @Schema(description = "대회 종료 시간")
        val endAt: LocalDateTime,
        @Schema(description = "대회 유형")
        val type: ContestType,
        @Schema(description = "초대 코드 (비공개 대회)")
        val inviteCode: String?,
        @Schema(description = "채점 방식")
        val scoringType: ScoringType,
        @Schema(description = "스코어보드 유형")
        val scoreboardType: ScoreboardType,
        @Schema(description = "스코어보드 프리즈 시간 (분)")
        val freezeMinutes: Int?,
        @Schema(description = "레이팅 반영 여부")
        val isRated: Boolean,
        @Schema(description = "대회 문제 목록")
        val problems: List<Problem>,
        @Schema(description = "참가자 수")
        val participantCount: Int,
        @Schema(description = "생성일시")
        val createdAt: LocalDateTime?,
        @Schema(description = "수정일시")
        val updatedAt: LocalDateTime?,
    )
}
