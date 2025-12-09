package kr.solve.infra.judge

import kotlinx.coroutines.reactor.awaitSingleOrNull
import kr.solve.domain.contest.domain.entity.Contest
import kr.solve.domain.problem.domain.entity.Problem
import kr.solve.domain.submission.domain.entity.Submission
import kr.solve.domain.submission.domain.enums.JudgeResult
import kr.solve.domain.submission.domain.enums.SubmissionStatus
import kr.solve.domain.user.domain.entity.User
import org.springframework.data.redis.core.ReactiveStringRedisTemplate
import org.springframework.stereotype.Component
import tools.jackson.databind.json.JsonMapper

@Component
class SubmissionEventPublisher(
    private val redisTemplate: ReactiveStringRedisTemplate,
    private val jsonMapper: JsonMapper,
) {
    suspend fun publishNew(
        submission: Submission,
        problem: Problem,
        contest: Contest?,
        user: User,
    ) {
        publish("NEW", submission.toEventData(problem, contest, user))
    }

    suspend fun publishUpdate(
        submission: Submission,
        problem: Problem,
        contest: Contest?,
        user: User,
        status: SubmissionStatus,
        result: JudgeResult? = null,
        score: Int? = null,
        time: Int? = null,
        memory: Int? = null,
    ) {
        publish(
            "UPDATE",
            SubmissionEvent.Data(
                id = submission.id,
                problem = SubmissionEvent.Problem(problem.id, problem.title),
                contest = contest?.let { SubmissionEvent.Contest(it.id, it.title) },
                user = SubmissionEvent.User(user.id, user.username, user.displayName, user.profileImage),
                language = submission.language,
                status = status,
                result = result,
                score = score,
                time = time,
                memory = memory,
                createdAt = submission.createdAt,
            ),
        )
    }

    private suspend fun publish(
        type: String,
        data: SubmissionEvent.Data,
    ) {
        val message = jsonMapper.writeValueAsString(SubmissionEvent(type, data))
        redisTemplate.convertAndSend(CHANNEL, message).awaitSingleOrNull()
    }

    private fun Submission.toEventData(
        problem: Problem,
        contest: Contest?,
        user: User,
    ) = SubmissionEvent.Data(
        id = id,
        problem = SubmissionEvent.Problem(problem.id, problem.title),
        contest = contest?.let { SubmissionEvent.Contest(it.id, it.title) },
        user = SubmissionEvent.User(user.id, user.username, user.displayName, user.profileImage),
        language = language,
        status = status,
        result = result,
        score = score,
        time = timeUsed,
        memory = memoryUsed,
        createdAt = createdAt,
    )

    companion object {
        const val CHANNEL = "submissions:events"
    }
}
