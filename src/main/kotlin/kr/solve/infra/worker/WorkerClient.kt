package kr.solve.infra.worker

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.reactive.awaitSingle
import kr.solve.infra.judge.JudgeEvent
import kr.solve.infra.judge.JudgeRequest
import org.springframework.data.redis.core.ReactiveStringRedisTemplate
import org.springframework.stereotype.Component
import tools.jackson.databind.json.JsonMapper

@Component
class WorkerClient(
    private val redisTemplate: ReactiveStringRedisTemplate,
    private val jsonMapper: JsonMapper,
) {
    companion object {
        private const val JUDGE_QUEUE = "solve:jobs:judge"
        private const val EXECUTE_QUEUE = "solve:jobs:execute"
        private const val EXECUTE_COMMAND_CHANNEL = "solve:execute:commands"

        private fun judgeStreamKey(submissionId: String) = "solve:judge:stream:$submissionId"
        private fun executeStreamKey(executionId: String) = "solve:execute:stream:$executionId"
    }

    fun startJudge(job: JudgeRequest): Flow<JudgeEvent> = flow {
        val streamKey = judgeStreamKey(job.submissionId.toString())
        val payload = jsonMapper.writeValueAsString(job)

        redisTemplate.opsForList()
            .rightPush(JUDGE_QUEUE, payload)
            .awaitSingle()

        var lastId = "0"
        while (true) {
            val records = redisTemplate.opsForStream<String, String>()
                .read(
                    org.springframework.data.redis.connection.stream.StreamOffset.create(
                        streamKey,
                        org.springframework.data.redis.connection.stream.ReadOffset.from(lastId)
                    )
                )
                .collectList()
                .awaitSingle()

            if (records.isEmpty()) {
                delay(10)
                continue
            }

            for (record in records) {
                lastId = record.id.value
                val data = record.value["data"] ?: continue
                val event = runCatching {
                    jsonMapper.readValue(data, JudgeEvent::class.java)
                }.getOrNull() ?: continue

                emit(event)

                if (event is JudgeEvent.Complete) {
                    redisTemplate.delete(streamKey).awaitSingle()
                    return@flow
                }
            }
        }
    }

    fun startExecution(job: ExecuteRequest): Flow<ExecuteEvent> = flow {
        val streamKey = executeStreamKey(job.executionId.toString())
        val payload = jsonMapper.writeValueAsString(job)

        redisTemplate.opsForList()
            .rightPush(EXECUTE_QUEUE, payload)
            .awaitSingle()

        var lastId = "0"
        while (true) {
            val records = redisTemplate.opsForStream<String, String>()
                .read(
                    org.springframework.data.redis.connection.stream.StreamOffset.create(
                        streamKey,
                        org.springframework.data.redis.connection.stream.ReadOffset.from(lastId)
                    )
                )
                .collectList()
                .awaitSingle()

            if (records.isEmpty()) {
                delay(10)
                continue
            }

            for (record in records) {
                lastId = record.id.value
                val data = record.value["data"] ?: continue
                val event = runCatching {
                    jsonMapper.readValue(data, ExecuteEvent::class.java)
                }.getOrNull() ?: continue

                emit(event)

                if (event is ExecuteEvent.Complete || event is ExecuteEvent.Error) {
                    redisTemplate.delete(streamKey).awaitSingle()
                    return@flow
                }
            }
        }
    }

    suspend fun sendExecuteCommand(executionId: String, command: ExecuteCommand) {
        val channel = "$EXECUTE_COMMAND_CHANNEL:$executionId"
        val payload = jsonMapper.writeValueAsString(command)
        redisTemplate.convertAndSend(channel, payload).awaitSingle()
    }
}
