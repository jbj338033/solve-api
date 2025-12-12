package kr.solve.domain.execution.presentation.websocket

import kr.solve.domain.submission.domain.enums.Language

data class ExecutionMessage(
    val type: Type,
    val data: Any? = null,
) {
    enum class Type {
        INIT,
        STDIN,
        KILL,
        READY,
        STDOUT,
        STDERR,
        COMPLETE,
        ERROR,
    }

    data class InitData(
        val problemId: Long,
        val language: Language,
        val code: String,
    )
}
