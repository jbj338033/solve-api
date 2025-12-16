package kr.solve.domain.submission.application.service

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.withContext
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
import kr.solve.domain.submission.domain.enums.Language
import kr.solve.domain.submission.domain.enums.SubmissionStatus
import kr.solve.domain.submission.domain.error.SubmissionError
import kr.solve.domain.submission.domain.repository.SubmissionQueryRepository
import kr.solve.domain.submission.domain.repository.SubmissionRepository
import kr.solve.domain.submission.domain.repository.SubmissionResultRepository
import kr.solve.domain.submission.presentation.response.SubmissionResponse
import kr.solve.domain.user.domain.entity.User
import kr.solve.domain.user.domain.repository.UserActivityRepository
import kr.solve.domain.user.domain.repository.UserRepository
import kr.solve.global.error.BusinessException
import kr.solve.infra.judge.JudgeEvent
import kr.solve.infra.judge.JudgeRequest
import kr.solve.infra.judge.JudgeService
import kr.solve.infra.judge.SubmissionEventPublisher
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.LocalDateTime

private val log = KotlinLogging.logger {}

@Service
class SubmissionService(
    private val submissionRepository: SubmissionRepository,
    private val submissionQueryRepository: SubmissionQueryRepository,
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
    suspend fun getSubmissions(
        cursor: Long?,
        limit: Int,
        username: String? = null,
        problemId: Long? = null,
        language: Language? = null,
        result: JudgeResult? = null,
    ): CursorPage<SubmissionResponse.Summary> {
        val userId = username?.let { userRepository.findByUsername(it)?.id!! }

        val submissions = submissionQueryRepository.findWithFilters(
            cursor = cursor,
            limit = limit + 1,
            userId = userId,
            problemId = problemId,
            language = language,
            result = result,
        ).toList()

        if (submissions.isEmpty()) return CursorPage(emptyList(), false)

        val problemIds = submissions.map { it.problemId }.distinct()
        val contestIds = submissions.mapNotNull { it.contestId }.distinct()
        val userIds = submissions.map { it.userId }.distinct()

        val problemMap = problemRepository.findAllByIdIn(problemIds).toList().associateBy { it.id!! }
        val contestMap = if (contestIds.isNotEmpty()) {
            contestRepository.findAllByIdIn(contestIds).toList().associateBy { it.id!! }
        } else emptyMap()
        val userMap = userRepository.findAllByIdIn(userIds).toList().associateBy { it.id!! }

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

    suspend fun getSubmission(submissionId: Long): SubmissionResponse.Detail {
        val submission = submissionRepository.findById(submissionId)
            ?: throw BusinessException(SubmissionError.NOT_FOUND)
        val problem = problemRepository.findById(submission.problemId)
            ?: throw BusinessException(SubmissionError.PROBLEM_NOT_FOUND)
        val contest = submission.contestId?.let { contestRepository.findById(it) }
        val user = userRepository.findById(submission.userId)
            ?: throw BusinessException(SubmissionError.USER_NOT_FOUND)

        return submission.toDetail(problem, contest, user)
    }

    suspend fun startJudge(
        userId: Long,
        problemId: Long,
        contestId: Long?,
        language: Language,
        code: String,
    ): Pair<Long, Flow<JudgeEvent>> {
        val user = userRepository.findById(userId)
            ?: throw BusinessException(SubmissionError.USER_NOT_FOUND)

        val problem = problemRepository.findById(problemId)
            ?: throw BusinessException(SubmissionError.PROBLEM_NOT_FOUND)

        if (!problem.isPublic && problem.authorId != userId) {
            throw BusinessException(SubmissionError.PROBLEM_ACCESS_DENIED)
        }

        val contest = contestId?.let { id ->
            val c = contestRepository.findById(id)
                ?: throw BusinessException(SubmissionError.NOT_FOUND)
            val now = LocalDateTime.now()
            if (now.isBefore(c.startAt)) throw BusinessException(SubmissionError.CONTEST_NOT_STARTED)
            if (now.isAfter(c.endAt)) throw BusinessException(SubmissionError.CONTEST_ENDED)
            if (!contestParticipantRepository.existsByContestIdAndUserId(id, userId)) {
                throw BusinessException(SubmissionError.NOT_PARTICIPATING)
            }
            c
        }

        val testcases = problemTestCaseRepository
            .findAllByProblemIdOrderByOrder(problem.id!!)
            .toList()
            .map { JudgeRequest.TestCase(id = it.id!!, input = it.input, output = it.output, order = it.order) }

        val submission = submissionRepository.save(
            Submission(
                problemId = problem.id!!,
                userId = userId,
                contestId = contestId,
                language = language,
                code = code,
            )
        )

        submissionEventPublisher.publishNew(submission, problem, contest, user)

        val events = flow {
            try {
                submissionRepository.updateStatus(submission.id!!, SubmissionStatus.JUDGING)
                submissionEventPublisher.publishUpdate(submission, problem, contest, user, SubmissionStatus.JUDGING)

                var finalResult: JudgeResult = JudgeResult.INTERNAL_ERROR
                var finalScore = 0
                var finalTime = 0
                var finalMemory = 0
                var finalError: String? = null

                judgeService.judge(
                    JudgeRequest(
                        submissionId = submission.id!!,
                        language = language,
                        code = code,
                        timeLimit = problem.timeLimit,
                        memoryLimit = problem.memoryLimit,
                        testcases = testcases,
                    )
                ).collect { event ->
                    when (event) {
                        is JudgeEvent.Progress -> {
                            submissionResultRepository.save(
                                SubmissionResult(
                                    submissionId = submission.id!!,
                                    testcaseId = event.testcaseId,
                                    result = event.result,
                                    timeUsed = event.time,
                                    memoryUsed = event.memory,
                                )
                            )
                            submissionEventPublisher.publishUpdate(
                                submission, problem, contest, user,
                                SubmissionStatus.JUDGING, score = event.score,
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
                    emit(event)
                }

                withContext(NonCancellable) {
                    submissionRepository.updateResult(
                        submission.id!!, SubmissionStatus.COMPLETED,
                        finalResult, finalScore, finalTime, finalMemory, finalError,
                    )
                    submissionEventPublisher.publishUpdate(
                        submission, problem, contest, user, SubmissionStatus.COMPLETED,
                        finalResult, finalScore, finalTime, finalMemory,
                    )
                    updateUserActivity(submission, finalResult)
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                log.error(e) { "Failed to judge submission ${submission.id}" }
                withContext(NonCancellable) {
                    submissionRepository.updateResult(
                        submission.id!!, SubmissionStatus.COMPLETED,
                        JudgeResult.INTERNAL_ERROR, 0, 0, 0, e.message,
                    )
                    submissionEventPublisher.publishUpdate(
                        submission, problem, contest, user, SubmissionStatus.COMPLETED,
                        JudgeResult.INTERNAL_ERROR, 0, 0, 0,
                    )
                }
                emit(JudgeEvent.Complete(JudgeResult.INTERNAL_ERROR, 0, 0, 0, e.message))
            }
        }

        return submission.id!! to events
    }

    private suspend fun updateUserActivity(submission: Submission, result: JudgeResult) {
        try {
            val today = LocalDate.now()
            val isFirstAc = result == JudgeResult.ACCEPTED &&
                !submissionRepository.existsByUserIdAndProblemIdAndResult(
                    submission.userId, submission.problemId, JudgeResult.ACCEPTED,
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
            log.warn(e) { "Failed to update user activity for submission ${submission.id}" }
        }
    }

    private fun Submission.toSummary(problem: Problem, contest: Contest?, user: User) =
        SubmissionResponse.Summary(
            id = id!!,
            problem = SubmissionResponse.Problem(problem.id!!, problem.title),
            contest = contest?.let { SubmissionResponse.Contest(it.id!!, it.title) },
            user = SubmissionResponse.User(user.id!!, user.username, user.displayName, user.profileImage),
            language = language,
            status = status,
            result = result,
            score = score,
            timeUsed = timeUsed,
            memoryUsed = memoryUsed,
            createdAt = createdAt,
        )

    private fun Submission.toDetail(problem: Problem, contest: Contest?, user: User) =
        SubmissionResponse.Detail(
            id = id!!,
            problem = SubmissionResponse.Problem(problem.id!!, problem.title),
            contest = contest?.let { SubmissionResponse.Contest(it.id!!, it.title) },
            user = SubmissionResponse.User(user.id!!, user.username, user.displayName, user.profileImage),
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
