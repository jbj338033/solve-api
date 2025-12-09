package kr.solve.domain.execution.presentation.websocket

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kr.solve.domain.execution.application.service.ExecutionService
import kr.solve.domain.submission.domain.enums.Language
import org.springframework.stereotype.Component
import org.springframework.web.reactive.socket.WebSocketHandler
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.publisher.Mono
import tools.jackson.databind.json.JsonMapper
import java.util.UUID

private val logger = KotlinLogging.logger {}

@Component
class ExecutionWebSocketHandler(
    private val executionService: ExecutionService,
    private val jsonMapper: JsonMapper,
) : WebSocketHandler {
    override fun handle(session: WebSocketSession): Mono<Void> {
        val scope = CoroutineScope(Dispatchers.IO)
        val inputChannel = Channel<String>(Channel.BUFFERED)
        var initialized = false
        var executionJob: kotlinx.coroutines.Job? = null

        return session
            .receive()
            .map { it.payloadAsText }
            .doOnNext { payload ->
                try {
                    val msg = jsonMapper.readValue(payload, ExecutionMessage::class.java)
                    when (msg.type) {
                        ExecutionMessage.Type.INIT -> {
                            if (initialized) return@doOnNext
                            initialized = true

                            val initData =
                                parseInitData(msg.data) ?: run {
                                    sendMessage(session, ExecutionMessage(ExecutionMessage.Type.ERROR, "Invalid init data"))
                                    return@doOnNext
                                }

                            executionJob =
                                scope.launch {
                                    try {
                                        executionService.execute(
                                            problemId = UUID.fromString(initData.problemId),
                                            language = Language.valueOf(initData.language),
                                            code = initData.code,
                                            inputChannel = inputChannel,
                                            onOutput = { sendMessage(session, ExecutionMessage(ExecutionMessage.Type.STDOUT, it)) },
                                            onError = { sendMessage(session, ExecutionMessage(ExecutionMessage.Type.STDERR, it)) },
                                            onComplete = {
                                                sendMessage(
                                                    session,
                                                    ExecutionMessage(
                                                        ExecutionMessage.Type.COMPLETE,
                                                        mapOf(
                                                            "exitCode" to it.exitCode,
                                                            "time" to it.time,
                                                            "memory" to it.memory / 1024,
                                                        ),
                                                    ),
                                                )
                                            },
                                        )
                                    } catch (e: Exception) {
                                        logger.error(e) { "Execution failed" }
                                        sendMessage(session, ExecutionMessage(ExecutionMessage.Type.ERROR, e.message ?: "Execution failed"))
                                    }
                                }
                        }
                        ExecutionMessage.Type.STDIN -> {
                            (msg.data as? String)?.let { inputChannel.trySend(it) }
                        }
                        ExecutionMessage.Type.KILL -> {
                            executionJob?.cancel()
                            inputChannel.close()
                        }
                        else -> Unit
                    }
                } catch (e: Exception) {
                    logger.error(e) { "Message handling error" }
                    sendMessage(session, ExecutionMessage(ExecutionMessage.Type.ERROR, e.message ?: "Message handling error"))
                }
            }.doOnError { logger.error(it) { "WebSocket error" } }
            .doFinally {
                inputChannel.close()
                scope.cancel()
            }.then()
    }

    private fun sendMessage(
        session: WebSocketSession,
        message: ExecutionMessage,
    ) {
        session.send(Mono.just(session.textMessage(jsonMapper.writeValueAsString(message)))).subscribe()
    }

    private fun parseInitData(data: Any?): ExecutionMessage.InitData? {
        val map = data as? Map<*, *> ?: return null
        return try {
            ExecutionMessage.InitData(
                problemId = map["problemId"] as? String ?: return null,
                language = map["language"] as? String ?: return null,
                code = map["code"] as? String ?: return null,
            )
        } catch (_: Exception) {
            null
        }
    }
}
