package kr.solve.domain.contest.application.service

import kotlinx.coroutines.flow.toList
import kr.solve.common.pagination.CursorPage
import kr.solve.domain.contest.domain.entity.Contest
import kr.solve.domain.contest.domain.entity.ContestParticipant
import kr.solve.domain.contest.domain.entity.ContestProblem
import kr.solve.domain.contest.domain.enums.ContestType
import kr.solve.domain.contest.domain.error.ContestError
import kr.solve.domain.contest.domain.repository.ContestParticipantRepository
import kr.solve.domain.contest.domain.repository.ContestProblemRepository
import kr.solve.domain.contest.domain.repository.ContestRepository
import kr.solve.domain.contest.presentation.request.CreateContestRequest
import kr.solve.domain.contest.presentation.request.JoinContestRequest
import kr.solve.domain.contest.presentation.request.UpdateContestRequest
import kr.solve.domain.contest.presentation.response.ContestResponse
import kr.solve.domain.contest.presentation.response.toDetail
import kr.solve.domain.contest.presentation.response.toSummary
import kr.solve.domain.problem.domain.repository.ProblemRepository
import kr.solve.domain.user.domain.enums.UserRole
import kr.solve.domain.user.domain.repository.UserRepository
import kr.solve.global.error.BusinessException
import kr.solve.global.security.userId
import kr.solve.global.security.userIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class ContestService(
    private val contestRepository: ContestRepository,
    private val contestProblemRepository: ContestProblemRepository,
    private val contestParticipantRepository: ContestParticipantRepository,
    private val problemRepository: ProblemRepository,
    private val userRepository: UserRepository,
) {
    suspend fun getContests(
        cursor: Long?,
        limit: Int,
    ): CursorPage<ContestResponse.Summary> {
        val contests = contestRepository.findAllByOrderByIdDesc(cursor, limit + 1).toList()
        return CursorPage.of(contests, limit) { it.toSummary() }
    }

    suspend fun getContest(contestId: Long): ContestResponse.Detail {
        val contest =
            contestRepository.findById(contestId)
                ?: throw BusinessException(ContestError.NOT_FOUND)

        val isStarted = LocalDateTime.now() >= contest.startAt
        val contestProblems =
            if (isStarted) contestProblemRepository.findAllByContestIdOrderByOrder(contestId).toList() else emptyList()
        val problemMap =
            if (isStarted) {
                problemRepository
                    .findAllByIdIn(contestProblems.map { it.problemId })
                    .toList()
                    .associateBy { it.id!! }
            } else {
                emptyMap()
            }

        val isParticipating =
            userIdOrNull()?.let { contestParticipantRepository.existsByContestIdAndUserId(contestId, it) } ?: false

        return contest.toDetail(contestProblems, problemMap, isParticipating)
    }

    @Transactional
    suspend fun createContest(request: CreateContestRequest) {
        if (request.endAt <= request.startAt) {
            throw BusinessException(ContestError.INVALID_TIME_RANGE)
        }

        val user = userRepository.findById(userId())
        if (request.isRated && user?.role != UserRole.ADMIN) {
            throw BusinessException(ContestError.RATED_CONTEST_FORBIDDEN)
        }

        val contest =
            contestRepository.save(
                Contest(
                    title = request.title,
                    description = request.description,
                    hostId = userId(),
                    startAt = request.startAt,
                    endAt = request.endAt,
                    type = request.type,
                    inviteCode = if (request.type == ContestType.PRIVATE) generateInviteCode() else null,
                    scoringType = request.scoringType,
                    scoreboardType = request.scoreboardType,
                    freezeMinutes = request.freezeMinutes,
                    isRated = request.isRated,
                ),
            )

        request.problems.forEachIndexed { index, problem ->
            contestProblemRepository.save(
                ContestProblem(
                    contestId = contest.id!!,
                    problemId = problem.problemId,
                    order = index,
                    score = problem.score,
                ),
            )
        }
    }

    @Transactional
    suspend fun updateContest(
        contestId: Long,
        request: UpdateContestRequest,
    ) {
        val contest =
            contestRepository.findById(contestId)
                ?: throw BusinessException(ContestError.NOT_FOUND)

        if (contest.hostId != userId()) {
            throw BusinessException(ContestError.FORBIDDEN)
        }

        val startAt = request.startAt ?: contest.startAt
        val endAt = request.endAt ?: contest.endAt
        if (endAt <= startAt) {
            throw BusinessException(ContestError.INVALID_TIME_RANGE)
        }

        val type = request.type ?: contest.type
        val inviteCode =
            when {
                type == ContestType.PUBLIC -> null
                contest.inviteCode != null -> contest.inviteCode
                else -> generateInviteCode()
            }

        contestRepository.save(
            contest.copy(
                title = request.title ?: contest.title,
                description = request.description ?: contest.description,
                startAt = startAt,
                endAt = endAt,
                type = type,
                inviteCode = inviteCode,
                scoringType = request.scoringType ?: contest.scoringType,
                scoreboardType = request.scoreboardType ?: contest.scoreboardType,
                freezeMinutes = request.freezeMinutes ?: contest.freezeMinutes,
            ),
        )

        request.problems?.let { problems ->
            contestProblemRepository.deleteAllByContestId(contestId)
            problems.forEachIndexed { index, problem ->
                contestProblemRepository.save(
                    ContestProblem(
                        contestId = contestId,
                        problemId = problem.problemId,
                        order = index,
                        score = problem.score,
                    ),
                )
            }
        }
    }

    @Transactional
    suspend fun deleteContest(contestId: Long) {
        val contest =
            contestRepository.findById(contestId)
                ?: throw BusinessException(ContestError.NOT_FOUND)

        if (contest.hostId != userId()) {
            throw BusinessException(ContestError.FORBIDDEN)
        }

        contestProblemRepository.deleteAllByContestId(contestId)
        contestRepository.deleteById(contestId)
    }

    @Transactional
    suspend fun joinContest(
        contestId: Long,
        request: JoinContestRequest,
    ) {
        val contest =
            contestRepository.findById(contestId)
                ?: throw BusinessException(ContestError.NOT_FOUND)

        val userId = userId()

        if (contest.type == ContestType.PRIVATE && request.inviteCode != contest.inviteCode) {
            throw BusinessException(ContestError.INVALID_INVITE_CODE)
        }

        if (contestParticipantRepository.existsByContestIdAndUserId(contest.id!!, userId)) {
            throw BusinessException(ContestError.ALREADY_PARTICIPATING)
        }

        contestParticipantRepository.save(ContestParticipant(contestId = contest.id!!, userId = userId))
    }

    @Transactional
    suspend fun leaveContest(contestId: Long) {
        val contest =
            contestRepository.findById(contestId)
                ?: throw BusinessException(ContestError.NOT_FOUND)

        val userId = userId()

        if (!contestParticipantRepository.existsByContestIdAndUserId(contest.id!!, userId)) {
            throw BusinessException(ContestError.NOT_PARTICIPATING)
        }

        contestParticipantRepository.deleteByContestIdAndUserId(contest.id!!, userId)
    }

    private fun generateInviteCode(): String = (1..8).map { INVITE_CODE_CHARS.random() }.joinToString("")

    companion object {
        private const val INVITE_CODE_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
    }
}
