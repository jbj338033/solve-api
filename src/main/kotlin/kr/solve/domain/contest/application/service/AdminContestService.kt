package kr.solve.domain.contest.application.service

import kotlinx.coroutines.flow.toList
import kr.solve.common.pagination.CursorPage
import kr.solve.domain.contest.domain.entity.ContestProblem
import kr.solve.domain.contest.domain.error.ContestError
import kr.solve.domain.contest.domain.repository.ContestParticipantRepository
import kr.solve.domain.contest.domain.repository.ContestProblemRepository
import kr.solve.domain.contest.domain.repository.ContestRepository
import kr.solve.domain.contest.presentation.request.UpdateContestRequest
import kr.solve.domain.contest.presentation.response.AdminContestResponse
import kr.solve.domain.contest.presentation.response.toAdminDetail
import kr.solve.domain.contest.presentation.response.toAdminSummary
import kr.solve.domain.problem.domain.repository.ProblemRepository
import kr.solve.global.error.BusinessException
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
    suspend fun updateContest(contestId: UUID, request: UpdateContestRequest): AdminContestResponse.Detail {
        val contest = contestRepository.findById(contestId)
            ?: throw BusinessException(ContestError.NOT_FOUND)

        val startAt = request.startAt ?: contest.startAt
        val endAt = request.endAt ?: contest.endAt
        if (endAt <= startAt) {
            throw BusinessException(ContestError.INVALID_TIME_RANGE)
        }

        contestRepository.save(
            contest.copy(
                title = request.title ?: contest.title,
                description = request.description ?: contest.description,
                startAt = startAt,
                endAt = endAt,
                type = request.type ?: contest.type,
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

        return getContest(contestId)
    }

    @Transactional
    suspend fun deleteContest(contestId: UUID) {
        val contest = contestRepository.findById(contestId)
            ?: throw BusinessException(ContestError.NOT_FOUND)

        contestProblemRepository.deleteAllByContestId(contestId)
        contestParticipantRepository.deleteAllByContestId(contestId)
        contestRepository.delete(contest)
    }
}
