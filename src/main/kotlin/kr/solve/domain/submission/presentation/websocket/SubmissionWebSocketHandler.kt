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

    @PostConstruct
    fun init() {
        container
            .receive(topic)
            .subscribe { message ->
                val json = message.message
                sessions.values.forEach { sink ->
                    sink.tryEmitNext(json)
                }
            }
        logger.info { "SubmissionWebSocketHandler initialized, subscribed to ${topic.topic}" }
    }

    @PreDestroy
    fun destroy() {
        container.destroyLater().subscribe()
    }

    override fun handle(session: WebSocketSession): Mono<Void> {
        val sink = Sinks.many().unicast().onBackpressureBuffer<String>()
        sessions[session.id] = sink

        val output = session.send(sink.asFlux().map { session.textMessage(it) })
        val input = session.receive().then()

        return Mono
            .zip(input, output)
            .doFinally { sessions.remove(session.id) }
            .then()
    }
}
