package kr.solve.domain.submission.presentation.websocket

import io.github.oshai.kotlinlogging.KotlinLogging
import kr.solve.infra.judge.SubmissionEvent
import kr.solve.infra.judge.SubmissionEventPublisher
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory
import org.springframework.data.redis.listener.ChannelTopic
import org.springframework.data.redis.listener.ReactiveRedisMessageListenerContainer
import org.springframework.stereotype.Component
import org.springframework.web.reactive.socket.WebSocketHandler
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.publisher.Mono
import tools.jackson.databind.json.JsonMapper

private val logger = KotlinLogging.logger {}

@Component
class SubmissionWebSocketHandler(
    private val connectionFactory: ReactiveRedisConnectionFactory,
    private val jsonMapper: JsonMapper,
) : WebSocketHandler {
    override fun handle(session: WebSocketSession): Mono<Void> {
        val container = ReactiveRedisMessageListenerContainer(connectionFactory)
        val topic = ChannelTopic(SubmissionEventPublisher.CHANNEL)

        val output =
            container
                .receive(topic)
                .mapNotNull { message ->
                    try {
                        val event = jsonMapper.readValue(message.message, SubmissionEvent::class.java)
                        session.textMessage(jsonMapper.writeValueAsString(event))
                    } catch (e: Exception) {
                        logger.error(e) { "Failed to parse message" }
                        null
                    }
                }

        return session
            .send(output)
            .and(session.receive().then())
            .doFinally { container.destroyLater().subscribe() }
    }
}
