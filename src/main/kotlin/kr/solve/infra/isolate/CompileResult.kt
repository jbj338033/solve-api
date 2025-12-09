package kr.solve.infra.isolate

data class CompileResult(
    val success: Boolean,
    val error: String?,
    val boxPath: String?,
    val boxId: Int?,
)
