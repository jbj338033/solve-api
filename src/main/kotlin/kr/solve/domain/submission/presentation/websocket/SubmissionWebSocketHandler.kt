package kr.solve.domain.submission.presentation.websocket

import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import kr.solve.infra.judge.SubmissionEventPublisher
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory
import org.springframework.data.redis.listener.ChannelTopic
import org.springframework.data.redis.listener.ReactiveRedisMessageListenerContainer
import org.springframework.stereotype.Component
import org.springframework.web.reactive.socket.WebSocketHandler
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.publisher.Mono
import reactor.core.publisher.Sinks
import java.util.concurrent.ConcurrentHashMap

private val logger = KotlinLogging.logger {}

@Component
class SubmissionWebSocketHandler(
    connectionFactory: ReactiveRedisConnectionFactory,
) : WebSocketHandler {
    private val container = ReactiveRedisMessageListenerContainer(connectionFactory)
    private val topic = ChannelTopic(SubmissionEventPublisher.CHANNEL)
    private val sessions = ConcurrentHashMap<String, Sinks.Many<String>>()

    init {
        logger.info { "SubmissionWebSocketHandler constructor called" }
    }

    @PostConstruct
    fun init() {
        logger.info { "SubmissionWebSocketHandler @PostConstruct called" }
        container
            .receive(topic)
            .doOnSubscribe { logger.info { "Redis subscription started for ${topic.topic}" } }
            .doOnNext { logger.info { "Redis message received: ${it.message.take(100)}" } }
            .doOnError { logger.error(it) { "Redis subscription error" } }
            .subscribe { message ->
                logger.info { "Broadcasting to ${sessions.size} sessions" }
                val json = message.message
                sessions.values.forEach { sink ->
                    sink.tryEmitNext(json)
                }
            }
        logger.info { "SubmissionWebSocketHandler initialized, subscribed to ${topic.topic}" }
    }

    @PreDestroy
    fun destroy() {
        logger.info { "SubmissionWebSocketHandler @PreDestroy called" }
        container.destroyLater().subscribe()
    }

    override fun handle(session: WebSocketSession): Mono<Void> {
        logger.info { "WebSocket handle() called: ${session.id}" }
        val sink = Sinks.many().unicast().onBackpressureBuffer<String>()
        sessions[session.id] = sink
        logger.info { "Session registered: ${session.id}, total sessions: ${sessions.size}" }

        val output = session.send(sink.asFlux().map { session.textMessage(it) })
        val input = session.receive().then()

        return Mono
            .zip(input, output)
            .doOnSubscribe { logger.info { "WebSocket Mono subscribed: ${session.id}" } }
            .doFinally {
                logger.info { "WebSocket disconnected: ${session.id}" }
                sessions.remove(session.id)
            }
            .then()
    }
}
