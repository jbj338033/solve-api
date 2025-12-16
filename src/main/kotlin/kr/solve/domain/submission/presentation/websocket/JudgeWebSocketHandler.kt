package kr.solve.domain.submission.presentation.websocket

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kr.solve.domain.submission.application.service.SubmissionService
import kr.solve.global.security.jwt.JwtProvider
import kr.solve.infra.judge.JudgeEvent
import org.springframework.stereotype.Component
import org.springframework.web.reactive.socket.WebSocketHandler
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.publisher.Mono
import reactor.core.publisher.Sinks
import tools.jackson.databind.json.JsonMapper
import java.util.concurrent.atomic.AtomicReference

private val logger = KotlinLogging.logger {}

@Component
class JudgeWebSocketHandler(
    private val submissionService: SubmissionService,
    private val jwtProvider: JwtProvider,
    private val jsonMapper: JsonMapper,
) : WebSocketHandler {
    private val judgeScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun handle(session: WebSocketSession): Mono<Void> {
        val sessionId = session.id
        logger.info { "[WS:$sessionId] Judge WebSocket connected" }

        val messageScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        val judgeJob = AtomicReference<Job?>(null)
        val sink = Sinks.many().unicast().onBackpressureBuffer<String>()

        val output = session.send(sink.asFlux().map { session.textMessage(it) })

        val input = session
            .receive()
            .map { it.payloadAsText }
            .doOnNext { payload ->
                logger.debug { "[WS:$sessionId] Received message: ${payload.take(200)}" }
                messageScope.launch {
                    handleMessage(sessionId, payload, judgeJob, sink)
                }
            }
            .doOnError { e ->
                if (e !is CancellationException) {
                    logger.error(e) { "[WS:$sessionId] WebSocket error" }
                }
            }
            .doFinally { signal ->
                logger.info { "[WS:$sessionId] Connection closed: signal=$signal" }
                judgeJob.get()?.cancel()
                sink.tryEmitComplete()
                messageScope.cancel()
            }
            .then()

        return output.and(input)
    }

    private suspend fun handleMessage(
        sessionId: String,
        payload: String,
        judgeJob: AtomicReference<Job?>,
        sink: Sinks.Many<String>,
    ) {
        try {
            val msg = jsonMapper.readValue(payload, JudgeMessage::class.java)
            logger.debug { "[WS:$sessionId] Message type: ${msg.type}" }
            when (msg.type) {
                JudgeMessage.Type.INIT -> handleInit(sessionId, msg, judgeJob, sink)
                else -> logger.warn { "[WS:$sessionId] Unknown message type: ${msg.type}" }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            logger.error(e) { "[WS:$sessionId] Message handling error" }
            emitMessage(sink, JudgeMessage(JudgeMessage.Type.ERROR, e.message ?: "Invalid message"))
        }
    }

    private suspend fun handleInit(
        sessionId: String,
        msg: JudgeMessage,
        judgeJob: AtomicReference<Job?>,
        sink: Sinks.Many<String>,
    ) {
        if (judgeJob.get() != null) {
            logger.warn { "[WS:$sessionId] INIT received but judge already running" }
            return
        }

        val initData = parseInitData(msg.data) ?: run {
            logger.warn { "[WS:$sessionId] Invalid init data" }
            emitMessage(sink, JudgeMessage(JudgeMessage.Type.ERROR, "Invalid init data"))
            return
        }

        logger.info { "[WS:$sessionId] INIT: problemId=${initData.problemId}, contestId=${initData.contestId}, language=${initData.language}" }

        val userId = initData.token?.let { extractUserId(it) } ?: run {
            logger.warn { "[WS:$sessionId] Authentication failed" }
            emitMessage(sink, JudgeMessage(JudgeMessage.Type.ERROR, "Authentication required"))
            return
        }

        logger.debug { "[WS:$sessionId] Authenticated userId=$userId" }

        try {
            val (submissionId, events) = submissionService.startJudge(
                userId = userId,
                problemId = initData.problemId,
                contestId = initData.contestId,
                language = initData.language,
                code = initData.code,
            )

            logger.info { "[WS:$sessionId] Judge started: submissionId=$submissionId" }
            emitMessage(sink, JudgeMessage(JudgeMessage.Type.CREATED, JudgeMessage.CreatedData(submissionId)))

            val job = judgeScope.launch {
                events.collect { event ->
                    val message = when (event) {
                        is JudgeEvent.Progress -> {
                            logger.debug { "[WS:$sessionId] Sending PROGRESS: testcase=${event.testcaseId}, result=${event.result}" }
                            JudgeMessage(
                                JudgeMessage.Type.PROGRESS,
                                JudgeMessage.ProgressData(
                                    testcaseId = event.testcaseId,
                                    result = event.result,
                                    time = event.time,
                                    memory = event.memory,
                                    score = event.score,
                                    progress = event.progress,
                                )
                            )
                        }
                        is JudgeEvent.Complete -> {
                            logger.info { "[WS:$sessionId] Sending COMPLETE: result=${event.result}, score=${event.score}" }
                            JudgeMessage(
                                JudgeMessage.Type.COMPLETE,
                                JudgeMessage.CompleteData(
                                    result = event.result,
                                    score = event.score,
                                    time = event.time,
                                    memory = event.memory,
                                    error = event.error,
                                )
                            )
                        }
                    }
                    emitMessage(sink, message)
                }
            }
            judgeJob.set(job)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            logger.error(e) { "[WS:$sessionId] Judge start failed" }
            emitMessage(sink, JudgeMessage(JudgeMessage.Type.ERROR, e.message ?: "Judge failed"))
        }
    }

    private fun emitMessage(sink: Sinks.Many<String>, message: JudgeMessage) {
        val json = jsonMapper.writeValueAsString(message)
        sink.tryEmitNext(json)
    }

    private fun parseInitData(data: Any?): JudgeMessage.InitData? =
        runCatching {
            jsonMapper.convertValue(data, JudgeMessage.InitData::class.java)
        }.getOrNull()

    private fun extractUserId(token: String): Long? =
        if (jwtProvider.validateToken(token) && jwtProvider.getType(token) == JwtProvider.JwtType.ACCESS) {
            runCatching { jwtProvider.getUserId(token) }.getOrNull()
        } else null
}
