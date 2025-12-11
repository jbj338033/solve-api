package kr.solve.domain.contest.application.service

import kotlinx.coroutines.flow.toList
import kr.solve.common.pagination.CursorPage
import kr.solve.domain.contest.domain.entity.Contest
import kr.solve.domain.contest.domain.entity.ContestProblem
import kr.solve.domain.contest.domain.enums.ContestType
import kr.solve.domain.contest.domain.error.ContestError
import kr.solve.domain.contest.domain.repository.ContestParticipantRepository
import kr.solve.domain.contest.domain.repository.ContestProblemRepository
import kr.solve.domain.contest.domain.repository.ContestRepository
import kr.solve.domain.contest.presentation.request.AdminContestProblemRequest
import kr.solve.domain.contest.presentation.request.AdminCreateContestRequest
import kr.solve.domain.contest.presentation.request.AdminUpdateContestRequest
import kr.solve.domain.contest.presentation.response.AdminContestResponse
import kr.solve.domain.contest.presentation.response.toAdminDetail
import kr.solve.domain.contest.presentation.response.toAdminSummary
import kr.solve.domain.problem.domain.repository.ProblemRepository
import kr.solve.global.error.BusinessException
import kr.solve.global.security.userId
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class AdminContestService(
    private val contestRepository: ContestRepository,
    private val contestProblemRepository: ContestProblemRepository,
    private val contestParticipantRepository: ContestParticipantRepository,
    private val problemRepository: ProblemRepository,
) {
    suspend fun getContests(
        cursor: UUID?,
        limit: Int,
    ): CursorPage<AdminContestResponse.Summary> {
        val contests = contestRepository.findAllByOrderByIdDesc(cursor, limit + 1).toList()
        return CursorPage.of(contests, limit) { it.toAdminSummary() }
    }

    suspend fun getContest(contestId: UUID): AdminContestResponse.Detail {
        val contest = contestRepository.findById(contestId)
            ?: throw BusinessException(ContestError.NOT_FOUND)

        val contestProblems = contestProblemRepository.findAllByContestIdOrderByOrder(contestId).toList()
        val problemMap = problemRepository.findAllByIdIn(contestProblems.map { it.problemId }).toList().associateBy { it.id }
        val participantCount = contestParticipantRepository.countByContestId(contestId)

        return contest.toAdminDetail(contestProblems, problemMap, participantCount.toInt())
    }

    @Transactional
    suspend fun createContest(request: AdminCreateContestRequest) {
        if (request.endAt <= request.startAt) {
            throw BusinessException(ContestError.INVALID_TIME_RANGE)
        }

        val contest = contestRepository.save(
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

        saveProblems(contest.id, request.problems)
    }

    @Transactional
    suspend fun updateContest(contestId: UUID, request: AdminUpdateContestRequest) {
        val contest = contestRepository.findById(contestId)
            ?: throw BusinessException(ContestError.NOT_FOUND)

        val startAt = request.startAt ?: contest.startAt
        val endAt = request.endAt ?: contest.endAt
        if (endAt <= startAt) {
            throw BusinessException(ContestError.INVALID_TIME_RANGE)
        }

        val type = request.type ?: contest.type
        val inviteCode = when {
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
            saveProblems(contestId, problems)
        }
    }

    @Transactional
    suspend fun deleteContest(contestId: UUID) {
        val contest = contestRepository.findById(contestId)
            ?: throw BusinessException(ContestError.NOT_FOUND)

        contestProblemRepository.deleteAllByContestId(contestId)
        contestParticipantRepository.deleteAllByContestId(contestId)
        contestRepository.delete(contest)
    }

    private suspend fun saveProblems(contestId: UUID, problems: List<AdminContestProblemRequest>) {
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

    private fun generateInviteCode(): String = (1..8).map { INVITE_CODE_CHARS.random() }.joinToString("")

    companion object {
        private const val INVITE_CODE_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
    }
}
