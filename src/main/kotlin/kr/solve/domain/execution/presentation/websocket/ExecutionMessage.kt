package kr.solve.domain.execution.presentation.websocket

data class ExecutionMessage(
    val type: Type,
    val data: Any? = null,
) {
    enum class Type {
        INIT,
        STDIN,
        STDOUT,
        STDERR,
        COMPLETE,
        ERROR,
        KILL,
    }

    data class InitData(
        val problemId: String,
        val language: String,
        val code: String,
    )
}
