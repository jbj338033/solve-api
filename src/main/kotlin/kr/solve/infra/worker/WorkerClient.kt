package kr.solve.infra.worker

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.reactive.awaitSingle
import kr.solve.infra.judge.JudgeEvent
import kr.solve.infra.judge.JudgeRequest
import org.springframework.data.redis.core.ReactiveStringRedisTemplate
import org.springframework.stereotype.Component
import tools.jackson.databind.json.JsonMapper

private val log = KotlinLogging.logger {}

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

    fun startJudge(job: JudgeRequest): Flow<JudgeEvent> =
        flow {
            val streamKey = judgeStreamKey(job.submissionId.toString())
            val payload = jsonMapper.writeValueAsString(job)
            log.info { "[Judge:${job.submissionId}] Submitting job: language=${job.language}, testcases=${job.testcases.size}, timeLimit=${job.timeLimit}ms, memoryLimit=${job.memoryLimit}MB" }

            redisTemplate
                .opsForList()
                .rightPush(JUDGE_QUEUE, payload)
                .awaitSingle()
            log.debug { "[Judge:${job.submissionId}] Job pushed to queue: $JUDGE_QUEUE" }

            var lastId = "0"
            var pollCount = 0
            while (true) {
                val records =
                    redisTemplate
                        .opsForStream<String, String>()
                        .read(
                            org.springframework.data.redis.connection.stream.StreamOffset.create(
                                streamKey,
                                org.springframework.data.redis.connection.stream.ReadOffset
                                    .from(lastId),
                            ),
                        ).collectList()
                        .awaitSingle()

                if (records.isEmpty()) {
                    pollCount++
                    if (pollCount % 100 == 0) {
                        log.debug { "[Judge:${job.submissionId}] Waiting for events from stream $streamKey (poll #$pollCount)" }
                    }
                    delay(10)
                    continue
                }

                for (record in records) {
                    lastId = record.id.value
                    val data = record.value["data"] ?: continue
                    val event =
                        runCatching {
                            jsonMapper.readValue(data, JudgeEvent::class.java)
                        }.onFailure { e ->
                            log.error(e) { "[Judge:${job.submissionId}] Failed to parse JudgeEvent: $data" }
                        }.getOrNull() ?: continue

                    when (event) {
                        is JudgeEvent.Progress -> log.debug { "[Judge:${job.submissionId}] Progress: testcase=${event.testcaseId}, result=${event.result}, time=${event.time}ms, memory=${event.memory}KB, score=${event.score}, progress=${event.progress}%" }
                        is JudgeEvent.Complete -> log.info { "[Judge:${job.submissionId}] Complete: result=${event.result}, score=${event.score}, time=${event.time}ms, memory=${event.memory}KB, error=${event.error}" }
                    }

                    emit(event)

                    if (event is JudgeEvent.Complete) {
                        log.debug { "[Judge:${job.submissionId}] Cleaning up stream: $streamKey" }
                        redisTemplate.delete(streamKey).awaitSingle()
                        return@flow
                    }
                }
            }
        }

    fun startExecution(job: ExecuteRequest): Flow<ExecuteEvent> =
        flow {
            val streamKey = executeStreamKey(job.executionId.toString())
            val payload = jsonMapper.writeValueAsString(job)

            redisTemplate
                .opsForList()
                .rightPush(EXECUTE_QUEUE, payload)
                .awaitSingle()

            var lastId = "0"
            while (true) {
                val records =
                    redisTemplate
                        .opsForStream<String, String>()
                        .read(
                            org.springframework.data.redis.connection.stream.StreamOffset.create(
                                streamKey,
                                org.springframework.data.redis.connection.stream.ReadOffset
                                    .from(lastId),
                            ),
                        ).collectList()
                        .awaitSingle()

                if (records.isEmpty()) {
                    delay(10)
                    continue
                }

                for (record in records) {
                    lastId = record.id.value
                    val data = record.value["data"] ?: continue
                    val event =
                        runCatching {
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

    suspend fun sendExecuteCommand(
        executionId: String,
        command: ExecuteCommand,
    ) {
        val channel = "$EXECUTE_COMMAND_CHANNEL:$executionId"
        val payload = jsonMapper.writeValueAsString(command)
        redisTemplate.convertAndSend(channel, payload).awaitSingle()
    }
}
