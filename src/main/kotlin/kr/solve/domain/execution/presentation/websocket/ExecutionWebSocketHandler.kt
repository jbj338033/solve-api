package kr.solve.domain.execution.presentation.websocket

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kr.solve.domain.execution.application.service.ExecutionService
import kr.solve.domain.submission.domain.enums.Language
import kr.solve.infra.worker.ExecuteEvent
import org.springframework.stereotype.Component
import org.springframework.web.reactive.socket.WebSocketHandler
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.publisher.Mono
import reactor.core.publisher.Sinks
import tools.jackson.databind.json.JsonMapper
import java.util.UUID
import java.util.concurrent.atomic.AtomicReference

private val logger = KotlinLogging.logger {}

@Component
class ExecutionWebSocketHandler(
    private val executionService: ExecutionService,
    private val jsonMapper: JsonMapper,
) : WebSocketHandler {
    override fun handle(session: WebSocketSession): Mono<Void> {
        val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        val executionIdRef = AtomicReference<UUID?>(null)
        val eventJob = AtomicReference<Job?>(null)
        val sink = Sinks.many().unicast().onBackpressureBuffer<String>()

        val output = session.send(sink.asFlux().map { session.textMessage(it) })

        val input =
            session
                .receive()
                .map { it.payloadAsText }
                .doOnNext { payload ->
                    scope.launch {
                        handleMessage(session, payload, executionIdRef, eventJob, sink, scope)
                    }
                }.doOnError { e ->
                    if (e !is CancellationException) {
                        logger.error(e) { "WebSocket error" }
                    }
                }.doFinally {
                    runBlocking {
                        executionIdRef.get()?.let { execId ->
                            runCatching { executionService.killExecution(execId) }
                        }
                    }
                    eventJob.get()?.cancel()
                    sink.tryEmitComplete()
                    scope.cancel()
                }.then()

        return Mono.zip(input, output).then()
    }

    private suspend fun handleMessage(
        session: WebSocketSession,
        payload: String,
        executionIdRef: AtomicReference<UUID?>,
        eventJob: AtomicReference<Job?>,
        sink: Sinks.Many<String>,
        scope: CoroutineScope,
    ) {
        try {
            val msg = jsonMapper.readValue(payload, ExecutionMessage::class.java)
            when (msg.type) {
                ExecutionMessage.Type.INIT -> handleInit(msg, executionIdRef, eventJob, sink, scope)
                ExecutionMessage.Type.STDIN -> handleStdin(msg, executionIdRef)
                ExecutionMessage.Type.KILL -> handleKill(executionIdRef)
                else -> Unit
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            logger.error(e) { "Message handling error" }
            emitMessage(sink, ExecutionMessage(ExecutionMessage.Type.ERROR, e.message ?: "Message handling error"))
        }
    }

    private suspend fun handleInit(
        msg: ExecutionMessage,
        executionIdRef: AtomicReference<UUID?>,
        eventJob: AtomicReference<Job?>,
        sink: Sinks.Many<String>,
        scope: CoroutineScope,
    ) {
        if (executionIdRef.get() != null) return

        val initData =
            parseInitData(msg.data) ?: run {
                emitMessage(sink, ExecutionMessage(ExecutionMessage.Type.ERROR, "Invalid init data"))
                return
            }

        try {
            val (execId, events) =
                executionService.startExecution(
                    problemId = UUID.fromString(initData.problemId),
                    language = Language.valueOf(initData.language),
                    code = initData.code,
                )
            executionIdRef.set(execId)

            val job =
                scope.launch {
                    try {
                        events.collect { event ->
                            if (!isActive) return@collect
                            val message =
                                when (event) {
                                    is ExecuteEvent.Ready -> ExecutionMessage(ExecutionMessage.Type.READY)
                                    is ExecuteEvent.Stdout -> ExecutionMessage(ExecutionMessage.Type.STDOUT, event.data)
                                    is ExecuteEvent.Stderr -> ExecutionMessage(ExecutionMessage.Type.STDERR, event.data)
                                    is ExecuteEvent.Complete ->
                                        ExecutionMessage(
                                            ExecutionMessage.Type.COMPLETE,
                                            mapOf("exitCode" to event.exitCode, "time" to event.time, "memory" to event.memory),
                                        )
                                    is ExecuteEvent.Error -> ExecutionMessage(ExecutionMessage.Type.ERROR, event.message)
                                }
                            emitMessage(sink, message)

                            if (event is ExecuteEvent.Complete || event is ExecuteEvent.Error) {
                                return@collect
                            }
                        }
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        logger.error(e) { "Event collection error" }
                    }
                }
            eventJob.set(job)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            logger.error(e) { "Execution failed" }
            emitMessage(sink, ExecutionMessage(ExecutionMessage.Type.ERROR, e.message ?: "Execution failed"))
        }
    }

    private suspend fun handleStdin(
        msg: ExecutionMessage,
        executionIdRef: AtomicReference<UUID?>,
    ) {
        val execId = executionIdRef.get() ?: return
        val data = msg.data as? String ?: return
        executionService.sendStdin(execId, data)
    }

    private suspend fun handleKill(executionIdRef: AtomicReference<UUID?>) {
        val execId = executionIdRef.get() ?: return
        executionService.killExecution(execId)
    }

    private fun emitMessage(
        sink: Sinks.Many<String>,
        message: ExecutionMessage,
    ) {
        val json = jsonMapper.writeValueAsString(message)
        sink.tryEmitNext(json)
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
