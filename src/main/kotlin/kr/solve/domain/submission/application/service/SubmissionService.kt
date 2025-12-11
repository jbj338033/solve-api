package kr.solve.domain.submission.application.service

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kr.solve.common.pagination.CursorPage
import kr.solve.domain.contest.domain.entity.Contest
import kr.solve.domain.contest.domain.repository.ContestParticipantRepository
import kr.solve.domain.contest.domain.repository.ContestRepository
import kr.solve.domain.problem.domain.entity.Problem
import kr.solve.domain.problem.domain.repository.ProblemRepository
import kr.solve.domain.problem.domain.repository.ProblemTestCaseRepository
import kr.solve.domain.submission.domain.entity.Submission
import kr.solve.domain.submission.domain.entity.SubmissionResult
import kr.solve.domain.submission.domain.enums.JudgeResult
import kr.solve.domain.submission.domain.enums.SubmissionStatus
import kr.solve.domain.submission.domain.error.SubmissionError
import kr.solve.domain.submission.domain.repository.SubmissionRepository
import kr.solve.domain.submission.domain.repository.SubmissionResultRepository
import kr.solve.domain.submission.presentation.request.CreateSubmissionRequest
import kr.solve.domain.submission.presentation.response.SubmissionResponse
import kr.solve.domain.user.domain.entity.User
import kr.solve.domain.user.domain.repository.UserActivityRepository
import kr.solve.domain.user.domain.repository.UserRepository
import kr.solve.global.error.BusinessException
import kr.solve.global.security.userId
import kr.solve.infra.judge.JudgeEvent
import kr.solve.infra.judge.JudgeRequest
import kr.solve.infra.judge.JudgeService
import kr.solve.infra.judge.SubmissionEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

@Service
class SubmissionService(
    private val submissionRepository: SubmissionRepository,
    private val submissionResultRepository: SubmissionResultRepository,
    private val problemRepository: ProblemRepository,
    private val problemTestCaseRepository: ProblemTestCaseRepository,
    private val contestRepository: ContestRepository,
    private val contestParticipantRepository: ContestParticipantRepository,
    private val userRepository: UserRepository,
    private val userActivityRepository: UserActivityRepository,
    private val judgeService: JudgeService,
    private val submissionEventPublisher: SubmissionEventPublisher,
) {
    private val judgeScope = CoroutineScope(Dispatchers.IO)

    suspend fun getSubmissions(
        cursor: UUID?,
        limit: Int,
    ): CursorPage<SubmissionResponse.Summary> {
        val submissions = submissionRepository.findAllByOrderByIdDesc(cursor, limit + 1).toList()

        val problemIds = submissions.map { it.problemId }.distinct()
        val contestIds = submissions.mapNotNull { it.contestId }.distinct()
        val userIds = submissions.map { it.userId }.distinct()

        val problemMap = problemRepository.findAllByIdIn(problemIds).toList().associateBy { it.id }
        val contestMap =
            if (contestIds.isNotEmpty()) {
                contestRepository.findAllByIdIn(contestIds).toList().associateBy { it.id }
            } else {
                emptyMap()
            }
        val userMap = userRepository.findAllByIdIn(userIds).toList().associateBy { it.id }

        return CursorPage.of(submissions, limit) { submission ->
            val problem = problemMap[submission.problemId] ?: return@of null
            val user = userMap[submission.userId] ?: return@of null
            submission.toSummary(
                problem = problem,
                contest = submission.contestId?.let { contestMap[it] },
                user = user,
            )
        }
    }

    suspend fun getSubmission(submissionId: UUID): SubmissionResponse.Detail {
        val submission = findById(submissionId)
        val problem =
            problemRepository.findById(submission.problemId)
                ?: throw BusinessException(SubmissionError.PROBLEM_NOT_FOUND)
        val contest = submission.contestId?.let { contestRepository.findById(it) }
        val user =
            userRepository.findById(submission.userId)
                ?: throw BusinessException(SubmissionError.USER_NOT_FOUND)

        return submission.toDetail(problem, contest, user)
    }

    @Transactional
    suspend fun createSubmission(
        problemId: UUID,
        request: CreateSubmissionRequest,
    ): SubmissionResponse.Summary {
        val currentUserId = userId()
        val user =
            userRepository.findById(currentUserId)
                ?: throw BusinessException(SubmissionError.USER_NOT_FOUND)

        val problem =
            problemRepository.findById(problemId)
                ?: throw BusinessException(SubmissionError.PROBLEM_NOT_FOUND)

        if (!problem.isPublic && problem.authorId != currentUserId) {
            throw BusinessException(SubmissionError.PROBLEM_ACCESS_DENIED)
        }

        val testcases =
            problemTestCaseRepository
                .findAllByProblemIdOrderByOrder(problemId)
                .toList()
                .map { JudgeRequest.TestCase(id = it.id, input = it.input, output = it.output, order = it.order) }

        val submission =
            submissionRepository.save(
                Submission(
                    problemId = problemId,
                    userId = currentUserId,
                    language = request.language,
                    code = request.code,
                ),
            )

        submissionEventPublisher.publishNew(submission, problem, null, user)

        judgeScope.launch {
            executeJudge(submission, request, problem, null, user, testcases)
        }

        return submission.toSummary(problem, null, user)
    }

    @Transactional
    suspend fun createContestSubmission(
        contestId: UUID,
        problemId: UUID,
        request: CreateSubmissionRequest,
    ): SubmissionResponse.Summary {
        val currentUserId = userId()
        val user =
            userRepository.findById(currentUserId)
                ?: throw BusinessException(SubmissionError.USER_NOT_FOUND)

        val contest =
            contestRepository.findById(contestId)
                ?: throw BusinessException(SubmissionError.NOT_FOUND)

        val now = LocalDateTime.now()
        if (now.isBefore(contest.startAt)) {
            throw BusinessException(SubmissionError.CONTEST_NOT_STARTED)
        }
        if (now.isAfter(contest.endAt)) {
            throw BusinessException(SubmissionError.CONTEST_ENDED)
        }

        if (!contestParticipantRepository.existsByContestIdAndUserId(contestId, currentUserId)) {
            throw BusinessException(SubmissionError.NOT_PARTICIPATING)
        }

        val problem =
            problemRepository.findById(problemId)
                ?: throw BusinessException(SubmissionError.PROBLEM_NOT_FOUND)

        val testcases =
            problemTestCaseRepository
                .findAllByProblemIdOrderByOrder(problemId)
                .toList()
                .map { JudgeRequest.TestCase(id = it.id, input = it.input, output = it.output, order = it.order) }

        val submission =
            submissionRepository.save(
                Submission(
                    problemId = problemId,
                    userId = currentUserId,
                    contestId = contestId,
                    language = request.language,
                    code = request.code,
                ),
            )

        submissionEventPublisher.publishNew(submission, problem, contest, user)

        judgeScope.launch {
            executeJudge(submission, request, problem, contest, user, testcases)
        }

        return submission.toSummary(problem, contest, user)
    }

    private suspend fun executeJudge(
        submission: Submission,
        request: CreateSubmissionRequest,
        problem: Problem,
        contest: Contest?,
        user: User,
        testcases: List<JudgeRequest.TestCase>,
    ) {
        try {
            submissionRepository.updateStatus(submission.id, SubmissionStatus.JUDGING)
            submissionEventPublisher.publishUpdate(submission, problem, contest, user, SubmissionStatus.JUDGING)

            var finalResult: JudgeResult = JudgeResult.INTERNAL_ERROR
            var finalScore = 0
            var finalTime = 0
            var finalMemory = 0
            var finalError: String? = null

            judgeService
                .judge(
                    JudgeRequest(
                        submissionId = submission.id,
                        language = request.language,
                        code = request.code,
                        timeLimit = problem.timeLimit,
                        memoryLimit = problem.memoryLimit,
                        testcases = testcases,
                    ),
                ).collect { event ->
                    when (event) {
                        is JudgeEvent.Progress -> {
                            submissionResultRepository.save(
                                SubmissionResult(
                                    submissionId = submission.id,
                                    testcaseId = event.testcaseId,
                                    result = event.result,
                                    timeUsed = event.time,
                                    memoryUsed = event.memory,
                                ),
                            )
                            submissionEventPublisher.publishUpdate(
                                submission,
                                problem,
                                contest,
                                user,
                                SubmissionStatus.JUDGING,
                                score = event.score,
                            )
                        }
                        is JudgeEvent.Complete -> {
                            finalResult = event.result
                            finalScore = event.score
                            finalTime = event.time
                            finalMemory = event.memory
                            finalError = event.error
                        }
                    }
                }

            submissionRepository.updateResult(
                submission.id,
                SubmissionStatus.COMPLETED,
                finalResult,
                finalScore,
                finalTime,
                finalMemory,
                finalError,
            )
            submissionEventPublisher.publishUpdate(
                submission,
                problem,
                contest,
                user,
                SubmissionStatus.COMPLETED,
                finalResult,
                finalScore,
                finalTime,
                finalMemory,
            )

            val today = LocalDate.now()
            val isFirstAc =
                finalResult == JudgeResult.ACCEPTED &&
                    !submissionRepository.existsByUserIdAndProblemIdAndResult(
                        submission.userId,
                        submission.problemId,
                        JudgeResult.ACCEPTED,
                    )

            userActivityRepository.upsertActivity(
                userId = submission.userId,
                date = today,
                solvedCount = if (isFirstAc) 1 else 0,
                submissionCount = 1,
            )

            if (isFirstAc) {
                userRepository.updateStreak(submission.userId, today, today.minusDays(1))
            }
        } catch (e: Exception) {
            submissionRepository.updateResult(
                submission.id,
                SubmissionStatus.COMPLETED,
                JudgeResult.INTERNAL_ERROR,
                0,
                0,
                0,
                e.message,
            )
            submissionEventPublisher.publishUpdate(
                submission,
                problem,
                contest,
                user,
                SubmissionStatus.COMPLETED,
                JudgeResult.INTERNAL_ERROR,
                0,
                0,
                0,
            )
        }
    }

    private suspend fun findById(submissionId: UUID): Submission =
        submissionRepository.findById(submissionId)
            ?: throw BusinessException(SubmissionError.NOT_FOUND)

    private fun Submission.toSummary(
        problem: Problem,
        contest: Contest?,
        user: User,
    ) = SubmissionResponse.Summary(
        id = id,
        problem = SubmissionResponse.Problem(problem.id, problem.title),
        contest = contest?.let { SubmissionResponse.Contest(it.id, it.title) },
        user = SubmissionResponse.User(user.id, user.username, user.displayName, user.profileImage),
        language = language,
        status = status,
        result = result,
        score = score,
        timeUsed = timeUsed,
        memoryUsed = memoryUsed,
        createdAt = createdAt,
    )

    private fun Submission.toDetail(
        problem: Problem,
        contest: Contest?,
        user: User,
    ) = SubmissionResponse.Detail(
        id = id,
        problem = SubmissionResponse.Problem(problem.id, problem.title),
        contest = contest?.let { SubmissionResponse.Contest(it.id, it.title) },
        user = SubmissionResponse.User(user.id, user.username, user.displayName, user.profileImage),
        language = language,
        code = code,
        status = status,
        result = result,
        score = score,
        timeUsed = timeUsed,
        memoryUsed = memoryUsed,
        createdAt = createdAt,
        judgedAt = judgedAt,
    )
}
