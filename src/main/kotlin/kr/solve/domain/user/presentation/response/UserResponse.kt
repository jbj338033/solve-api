package kr.solve.domain.user.presentation.response

import io.swagger.v3.oas.annotations.media.Schema
import kr.solve.common.enums.Tier
import kr.solve.domain.banner.domain.entity.Banner
import kr.solve.domain.problem.domain.enums.ProblemDifficulty
import kr.solve.domain.submission.domain.enums.Language
import kr.solve.domain.user.domain.entity.User
import kr.solve.domain.user.domain.entity.UserActivity
import kr.solve.domain.user.domain.entity.UserRatingHistory
import kr.solve.domain.user.domain.enums.RatingType
import kr.solve.domain.user.domain.enums.UserOAuthProvider
import kr.solve.domain.user.domain.enums.UserRole
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

fun User.toMe(providers: List<UserOAuthProvider>) =
    UserResponse.Me(
        id = id,
        username = username,
        displayName = displayName,
        email = email,
        profileImage = profileImage,
        bio = bio,
        organization = organization,
        problemRating = problemRating,
        contestRating = contestRating,
        role = role,
        oauthProviders = providers,
        createdAt = createdAt,
    )

fun User.toProfile(banner: Banner?) =
    UserResponse.Profile(
        id = id,
        username = username,
        displayName = displayName,
        profileImage = profileImage,
        bio = bio,
        organization = organization,
        problemRating = problemRating,
        contestRating = contestRating,
        problemTier = Tier.fromRating(problemRating),
        contestTier = Tier.fromRating(contestRating),
        currentStreak = currentStreak,
        maxStreak = maxStreak,
        banner = banner?.let { UserResponse.Profile.Banner(it.id, it.name, it.imageUrl) },
        createdAt = createdAt,
    )

fun User.toRank(
    rank: Int,
    ratingType: RatingType,
): UserResponse.Rank {
    val rating =
        when (ratingType) {
            RatingType.PROBLEM -> problemRating
            RatingType.CONTEST -> contestRating
        }
    return UserResponse.Rank(
        rank = rank,
        id = id,
        username = username,
        displayName = displayName,
        profileImage = profileImage,
        rating = rating,
        tier = Tier.fromRating(rating),
    )
}

fun UserActivity.toResponse() =
    UserResponse.Activity(
        date = date,
        solvedCount = solvedCount,
        submissionCount = submissionCount,
    )

fun UserRatingHistory.toResponse() =
    UserResponse.RatingHistory(
        rating = rating,
        ratingType = ratingType,
        contestId = contestId,
        recordedAt = recordedAt,
    )

object UserResponse {
    @Schema(name = "User.Me", description = "내 정보")
    data class Me(
        @Schema(description = "사용자 ID")
        val id: UUID,
        @Schema(description = "사용자명", example = "johndoe")
        val username: String,
        @Schema(description = "표시 이름", example = "John Doe")
        val displayName: String,
        @Schema(description = "이메일", example = "john@example.com")
        val email: String,
        @Schema(description = "프로필 이미지 URL")
        val profileImage: String,
        @Schema(description = "자기소개")
        val bio: String,
        @Schema(description = "소속")
        val organization: String,
        @Schema(description = "문제 레이팅", example = "1500")
        val problemRating: Int,
        @Schema(description = "대회 레이팅", example = "1500")
        val contestRating: Int,
        @Schema(description = "사용자 권한")
        val role: UserRole,
        @Schema(description = "연동된 OAuth 제공자 목록")
        val oauthProviders: List<UserOAuthProvider>,
        @Schema(description = "가입일시")
        val createdAt: LocalDateTime?,
    )

    @Schema(name = "User.Profile", description = "사용자 프로필")
    data class Profile(
        @Schema(description = "사용자 ID")
        val id: UUID,
        @Schema(description = "사용자명", example = "johndoe")
        val username: String,
        @Schema(description = "표시 이름", example = "John Doe")
        val displayName: String,
        @Schema(description = "프로필 이미지 URL")
        val profileImage: String,
        @Schema(description = "자기소개")
        val bio: String,
        @Schema(description = "소속")
        val organization: String,
        @Schema(description = "문제 레이팅", example = "1500")
        val problemRating: Int,
        @Schema(description = "대회 레이팅", example = "1500")
        val contestRating: Int,
        @Schema(description = "문제 티어")
        val problemTier: Tier,
        @Schema(description = "대회 티어")
        val contestTier: Tier,
        @Schema(description = "현재 연속 풀이 일수", example = "7")
        val currentStreak: Int,
        @Schema(description = "최대 연속 풀이 일수", example = "30")
        val maxStreak: Int,
        @Schema(description = "선택한 배너")
        val banner: Banner?,
        @Schema(description = "가입일시")
        val createdAt: LocalDateTime?,
    ) {
        @Schema(name = "User.Profile.Banner", description = "프로필 배너")
        data class Banner(
            @Schema(description = "배너 ID")
            val id: UUID,
            @Schema(description = "배너 이름")
            val name: String,
            @Schema(description = "배너 이미지 URL")
            val imageUrl: String,
        )
    }

    @Schema(name = "User.Rank", description = "사용자 랭킹 정보")
    data class Rank(
        @Schema(description = "순위", example = "1")
        val rank: Int,
        @Schema(description = "사용자 ID")
        val id: UUID,
        @Schema(description = "사용자명", example = "johndoe")
        val username: String,
        @Schema(description = "표시 이름", example = "John Doe")
        val displayName: String,
        @Schema(description = "프로필 이미지 URL")
        val profileImage: String,
        @Schema(description = "레이팅", example = "1500")
        val rating: Int,
        @Schema(description = "티어")
        val tier: Tier,
    )

    @Schema(name = "User.Stats", description = "사용자 통계")
    data class Stats(
        @Schema(description = "해결한 문제 수", example = "42")
        val solvedCount: Int,
        @Schema(description = "총 제출 수", example = "100")
        val submissionCount: Int,
        @Schema(description = "난이도별 분포")
        val difficultyDistribution: Map<ProblemDifficulty, Int>,
        @Schema(description = "태그별 분포")
        val tagDistribution: List<TagStat>,
        @Schema(description = "언어별 분포")
        val languageDistribution: Map<Language, Int>,
    ) {
        @Schema(name = "User.Stats.TagStat", description = "태그별 통계")
        data class TagStat(
            @Schema(description = "태그 이름", example = "DP")
            val name: String,
            @Schema(description = "해결한 문제 수", example = "10")
            val count: Int,
        )
    }

    @Schema(name = "User.Activity", description = "일별 활동 기록")
    data class Activity(
        @Schema(description = "날짜")
        val date: LocalDate,
        @Schema(description = "해결한 문제 수", example = "3")
        val solvedCount: Int,
        @Schema(description = "제출 수", example = "5")
        val submissionCount: Int,
    )

    @Schema(name = "User.RatingHistory", description = "레이팅 변동 기록")
    data class RatingHistory(
        @Schema(description = "레이팅", example = "1500")
        val rating: Int,
        @Schema(description = "레이팅 타입")
        val ratingType: RatingType,
        @Schema(description = "대회 ID (대회 레이팅인 경우)")
        val contestId: UUID?,
        @Schema(description = "기록 일시")
        val recordedAt: LocalDateTime,
    )
}
