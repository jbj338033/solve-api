package kr.solve.global.websocket.config

import kr.solve.domain.execution.presentation.websocket.ExecutionWebSocketHandler
import kr.solve.domain.submission.presentation.websocket.JudgeWebSocketHandler
import kr.solve.domain.submission.presentation.websocket.SubmissionWebSocketHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.Ordered
import org.springframework.web.reactive.HandlerMapping
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter

@Configuration
class WebSocketConfig {
    @Bean
    fun webSocketHandlerMapping(
        executionWebSocketHandler: ExecutionWebSocketHandler,
        judgeWebSocketHandler: JudgeWebSocketHandler,
        submissionWebSocketHandler: SubmissionWebSocketHandler,
    ): HandlerMapping {
        val map = mapOf(
            "/ws/executions" to executionWebSocketHandler,
            "/ws/judge" to judgeWebSocketHandler,
            "/ws/submissions" to submissionWebSocketHandler,
        )
        return SimpleUrlHandlerMapping(map, Ordered.HIGHEST_PRECEDENCE)
    }

    @Bean
    fun webSocketHandlerAdapter(): WebSocketHandlerAdapter = WebSocketHandlerAdapter()
}
