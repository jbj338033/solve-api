package kr.solve.domain.user.application.service

import kotlinx.coroutines.flow.toList
import kr.solve.domain.auth.domain.error.AuthError
import kr.solve.domain.banner.domain.repository.BannerRepository
import kr.solve.domain.problem.domain.repository.ProblemRepository
import kr.solve.domain.problem.domain.repository.ProblemTagRepository
import kr.solve.domain.submission.domain.repository.SubmissionRepository
import kr.solve.domain.tag.domain.repository.TagRepository
import kr.solve.domain.user.domain.entity.UserOAuth
import kr.solve.domain.user.domain.entity.UserSettings
import kr.solve.domain.user.domain.enums.RatingType
import kr.solve.domain.user.domain.enums.UserOAuthProvider
import kr.solve.domain.user.domain.error.UserError
import kr.solve.domain.user.domain.repository.UserActivityRepository
import kr.solve.domain.user.domain.repository.UserOAuthRepository
import kr.solve.domain.user.domain.repository.UserRatingHistoryRepository
import kr.solve.domain.user.domain.repository.UserRepository
import kr.solve.domain.user.domain.repository.UserSettingsRepository
import kr.solve.domain.user.presentation.request.OAuthLinkRequest
import kr.solve.domain.user.presentation.request.UpdateUserSettingsRequest
import kr.solve.domain.user.presentation.response.UserResponse
import kr.solve.domain.user.presentation.response.toMe
import kr.solve.domain.user.presentation.response.toProfile
import kr.solve.domain.user.presentation.response.toRank
import kr.solve.domain.user.presentation.response.toResponse
import kr.solve.global.error.BusinessException
import kr.solve.global.security.userId
import kr.solve.infra.oauth.OAuthClient
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
class UserService(
    private val userRepository: UserRepository,
    private val userOAuthRepository: UserOAuthRepository,
    private val userActivityRepository: UserActivityRepository,
    private val userRatingHistoryRepository: UserRatingHistoryRepository,
    private val userSettingsRepository: UserSettingsRepository,
    private val bannerRepository: BannerRepository,
    private val submissionRepository: SubmissionRepository,
    private val problemRepository: ProblemRepository,
    private val problemTagRepository: ProblemTagRepository,
    private val tagRepository: TagRepository,
    private val oauthClients: Map<UserOAuthProvider, OAuthClient>,
) {
    suspend fun getMe(): UserResponse.Me {
        val userId = userId()
        val user =
            userRepository.findById(userId)
                ?: throw BusinessException(UserError.NOT_FOUND)
        val providers = userOAuthRepository.findAllByUserId(userId).map { it.provider }
        return user.toMe(providers)
    }

    suspend fun getProfile(username: String): UserResponse.Profile {
        val user =
            userRepository.findByUsername(username)
                ?: throw BusinessException(UserError.NOT_FOUND)
        val banner = user.selectedBannerId?.let { bannerRepository.findById(it) }
        val settings = userSettingsRepository.findById(user.id!!)
        return user.toProfile(banner, settings)
    }

    suspend fun getRanking(type: RatingType): List<UserResponse.Rank> {
        val users =
            when (type) {
                RatingType.PROBLEM -> userRepository.findAllByOrderByProblemRatingDesc()
                RatingType.CONTEST -> userRepository.findAllByOrderByContestRatingDesc()
            }.toList()

        return users.mapIndexed { index, user ->
            user.toRank(index + 1, type)
        }
    }

    @Transactional
    suspend fun linkOAuth(
        provider: UserOAuthProvider,
        request: OAuthLinkRequest,
    ): UserResponse.Me {
        val userId = userId()
        val client = oauthClients[provider] ?: throw BusinessException(AuthError.UNSUPPORTED_PROVIDER)
        val info = client.getUserInfo(request.credential)

        val existing = userOAuthRepository.findByProviderAndProviderId(provider, info.providerId)
        if (existing != null) {
            throw BusinessException(UserError.OAUTH_ALREADY_LINKED)
        }

        userOAuthRepository.save(UserOAuth(userId = userId, provider = provider, providerId = info.providerId))

        val user =
            userRepository.findById(userId)
                ?: throw BusinessException(UserError.NOT_FOUND)
        val providers = userOAuthRepository.findAllByUserId(userId).map { it.provider }
        return user.toMe(providers)
    }

    @Transactional
    suspend fun unlinkOAuth(provider: UserOAuthProvider): UserResponse.Me {
        val userId = userId()

        if (userOAuthRepository.countByUserId(userId) <= 1) {
            throw BusinessException(UserError.CANNOT_UNLINK_LAST_OAUTH)
        }

        userOAuthRepository.deleteByUserIdAndProvider(userId, provider)

        val user =
            userRepository.findById(userId)
                ?: throw BusinessException(UserError.NOT_FOUND)
        val providers = userOAuthRepository.findAllByUserId(userId).map { it.provider }
        return user.toMe(providers)
    }

    suspend fun getActivities(
        username: String,
        year: Int?,
    ): List<UserResponse.Activity> {
        val user =
            userRepository.findByUsername(username)
                ?: throw BusinessException(UserError.NOT_FOUND)

        val (startDate, endDate) =
            if (year != null) {
                LocalDate.of(year, 1, 1) to LocalDate.of(year, 12, 31)
            } else {
                val today = LocalDate.now()
                today.minusYears(1).plusDays(1) to today
            }

        return userActivityRepository
            .findAllByUserIdAndDateBetween(user.id!!, startDate, endDate)
            .toList()
            .map { it.toResponse() }
    }

    suspend fun getRatingHistory(
        username: String,
        type: RatingType,
    ): List<UserResponse.RatingHistory> {
        val user =
            userRepository.findByUsername(username)
                ?: throw BusinessException(UserError.NOT_FOUND)

        return userRatingHistoryRepository
            .findAllByUserIdAndRatingTypeOrderByRecordedAtDesc(user.id!!, type)
            .toList()
            .map { it.toResponse() }
    }

    suspend fun getStats(username: String): UserResponse.Stats {
        val user =
            userRepository.findByUsername(username)
                ?: throw BusinessException(UserError.NOT_FOUND)

        val solvedProblemIds = submissionRepository.findSolvedProblemIdsByUserId(user.id!!).toList()
        val submissionCount = submissionRepository.countByUserId(user.id!!)

        if (solvedProblemIds.isEmpty()) {
            val submissions = submissionRepository.findAllByUserIdOrderByIdDesc(user.id!!, null, 10000).toList()
            return UserResponse.Stats(
                solvedCount = 0,
                submissionCount = submissionCount.toInt(),
                difficultyDistribution = emptyMap(),
                tagDistribution = emptyList(),
                languageDistribution = submissions.groupingBy { it.language }.eachCount(),
            )
        }

        val problems = problemRepository.findAllByIdIn(solvedProblemIds).toList()
        val difficultyDistribution = problems.groupingBy { it.difficulty }.eachCount()

        val problemTags = problemTagRepository.findAllByProblemIdIn(solvedProblemIds).toList()
        val tagIds = problemTags.map { it.tagId }.distinct()
        val tags = if (tagIds.isEmpty()) emptyMap() else tagRepository.findAllByIdIn(tagIds).toList().associateBy { it.id }
        val tagDistribution =
            problemTags
                .groupingBy { it.tagId }
                .eachCount()
                .map { (tagId, count) -> UserResponse.Stats.TagStat(tags[tagId]?.name ?: "", count) }
                .filter { it.name.isNotEmpty() }
                .sortedByDescending { it.count }

        val submissions = submissionRepository.findAllByUserIdOrderByIdDesc(user.id!!, null, 10000).toList()
        val languageDistribution = submissions.groupingBy { it.language }.eachCount()

        return UserResponse.Stats(
            solvedCount = solvedProblemIds.size,
            submissionCount = submissionCount.toInt(),
            difficultyDistribution = difficultyDistribution,
            tagDistribution = tagDistribution,
            languageDistribution = languageDistribution,
        )
    }

    suspend fun getMySettings(): UserResponse.Settings {
        val userId = userId()
        val settings = userSettingsRepository.findById(userId)
            ?: UserSettings(userId = userId)
        return settings.toResponse()
    }

    @Transactional
    suspend fun updateMySettings(request: UpdateUserSettingsRequest): UserResponse.Settings {
        val userId = userId()
        val existing = userSettingsRepository.findById(userId)
        val settings = (existing ?: UserSettings(userId = userId)).let {
            it.copy(
                country = request.country ?: it.country,
                countryVisible = request.countryVisible ?: it.countryVisible,
                birthDate = request.birthDate ?: it.birthDate,
                birthDateVisible = request.birthDateVisible ?: it.birthDateVisible,
                gender = request.gender ?: it.gender,
                genderOther = request.genderOther ?: it.genderOther,
                genderVisible = request.genderVisible ?: it.genderVisible,
                pronouns = request.pronouns ?: it.pronouns,
                pronounsVisible = request.pronounsVisible ?: it.pronounsVisible,
            )
        }
        return userSettingsRepository.save(settings).toResponse()
    }

}
