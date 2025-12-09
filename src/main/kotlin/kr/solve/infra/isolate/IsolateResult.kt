package kr.solve.infra.isolate

data class IsolateResult(
    val success: Boolean,
    val stdout: String,
    val stderr: String,
    val exitCode: Int,
    val time: Int,
    val memory: Int,
    val status: IsolateStatus,
    val message: String?,
)
