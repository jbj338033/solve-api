package kr.solve.domain.submission.presentation.response

import io.swagger.v3.oas.annotations.media.Schema
import kr.solve.domain.submission.domain.enums.JudgeResult
import kr.solve.domain.submission.domain.enums.Language
import kr.solve.domain.submission.domain.enums.SubmissionStatus
import java.time.LocalDateTime

object SubmissionResponse {
    @Schema(name = "Submission.Problem", description = "제출 관련 문제 정보")
    data class Problem(
        @Schema(description = "문제 ID")
        val id: Long,
        @Schema(description = "문제 제목", example = "A+B")
        val title: String,
    )

    @Schema(name = "Submission.Contest", description = "제출 관련 대회 정보")
    data class Contest(
        @Schema(description = "대회 ID")
        val id: Long,
        @Schema(description = "대회 제목", example = "2024 신입생 대회")
        val title: String,
    )

    @Schema(name = "Submission.User", description = "제출자 정보")
    data class User(
        @Schema(description = "사용자 ID")
        val id: Long,
        @Schema(description = "사용자명", example = "johndoe")
        val username: String,
        @Schema(description = "표시 이름", example = "John Doe")
        val displayName: String,
        @Schema(description = "프로필 이미지 URL")
        val profileImage: String,
    )

    @Schema(name = "Submission.Summary", description = "제출 요약 정보")
    data class Summary(
        @Schema(description = "제출 ID")
        val id: Long,
        @Schema(description = "문제 정보")
        val problem: Problem,
        @Schema(description = "대회 정보 (대회 제출인 경우)")
        val contest: Contest?,
        @Schema(description = "제출자 정보")
        val user: User,
        @Schema(description = "제출 언어")
        val language: Language,
        @Schema(description = "제출 상태")
        val status: SubmissionStatus,
        @Schema(description = "채점 결과")
        val result: JudgeResult?,
        @Schema(description = "점수 (IOI 방식)", example = "100")
        val score: Int?,
        @Schema(description = "실행 시간 (ms)", example = "124")
        val timeUsed: Int?,
        @Schema(description = "메모리 사용량 (KB)", example = "12345")
        val memoryUsed: Int?,
        @Schema(description = "제출일시")
        val createdAt: LocalDateTime?,
    )

    @Schema(name = "Submission.Detail", description = "제출 상세 정보")
    data class Detail(
        @Schema(description = "제출 ID")
        val id: Long,
        @Schema(description = "문제 정보")
        val problem: Problem,
        @Schema(description = "대회 정보 (대회 제출인 경우)")
        val contest: Contest?,
        @Schema(description = "제출자 정보")
        val user: User,
        @Schema(description = "제출 언어")
        val language: Language,
        @Schema(description = "제출 코드")
        val code: String,
        @Schema(description = "제출 상태")
        val status: SubmissionStatus,
        @Schema(description = "채점 결과")
        val result: JudgeResult?,
        @Schema(description = "점수 (IOI 방식)", example = "100")
        val score: Int?,
        @Schema(description = "실행 시간 (ms)", example = "124")
        val timeUsed: Int?,
        @Schema(description = "메모리 사용량 (KB)", example = "12345")
        val memoryUsed: Int?,
        @Schema(description = "제출일시")
        val createdAt: LocalDateTime?,
        @Schema(description = "채점 완료일시")
        val judgedAt: LocalDateTime?,
    )
}
