package kr.solve.infra.judge

import kr.solve.domain.submission.domain.enums.JudgeResult
import kr.solve.infra.isolate.IsolateExecutor
import kr.solve.infra.isolate.IsolateStatus
import org.springframework.stereotype.Service

@Service
class JudgeService(
    private val isolateExecutor: IsolateExecutor,
) {
    suspend fun judge(request: JudgeRequest): JudgeResponse {
        val compileResult = isolateExecutor.compile(request.language, request.code)

        if (!compileResult.success) {
            return JudgeResponse(JudgeResult.COMPILE_ERROR, 0, 0, 0, compileResult.error, emptyList())
        }

        val boxId = requireNotNull(compileResult.boxId) { "boxId must not be null after successful compile" }
        val results = mutableListOf<JudgeResponse.TestCaseResult>()
        var overallResult = JudgeResult.ACCEPTED
        var maxTime = 0
        var maxMemory = 0

        try {
            for (testcase in request.testcases.sortedBy { it.order }) {
                val isolateResult =
                    isolateExecutor.runWithCompiledBox(
                        boxId,
                        request.language,
                        testcase.input,
                        request.timeLimit,
                        request.memoryLimit,
                    )

                val judgeResult =
                    when (isolateResult.status) {
                        IsolateStatus.OK ->
                            if (compareOutput(
                                    isolateResult.stdout,
                                    testcase.output,
                                )
                            ) {
                                JudgeResult.ACCEPTED
                            } else {
                                JudgeResult.WRONG_ANSWER
                            }
                        IsolateStatus.TIME_LIMIT_EXCEEDED -> JudgeResult.TIME_LIMIT_EXCEEDED
                        IsolateStatus.MEMORY_LIMIT_EXCEEDED -> JudgeResult.MEMORY_LIMIT_EXCEEDED
                        IsolateStatus.RUNTIME_ERROR -> JudgeResult.RUNTIME_ERROR
                        IsolateStatus.INTERNAL_ERROR -> JudgeResult.INTERNAL_ERROR
                    }

                val memory = isolateResult.memory / 1024
                results.add(JudgeResponse.TestCaseResult(testcase.id, judgeResult, isolateResult.time, memory))
                maxTime = maxOf(maxTime, isolateResult.time)
                maxMemory = maxOf(maxMemory, memory)

                if (judgeResult != JudgeResult.ACCEPTED) {
                    overallResult = judgeResult
                    break
                }
            }
        } finally {
            isolateExecutor.releaseBox(boxId)
        }

        val score = (results.count { it.result == JudgeResult.ACCEPTED } * 100) / request.testcases.size
        return JudgeResponse(overallResult, score, maxTime, maxMemory, null, results)
    }

    private fun compareOutput(
        actual: String,
        expected: String,
    ): Boolean =
        actual.trimEnd().lines().map {
            it.trimEnd()
        } == expected.trimEnd().lines().map { it.trimEnd() }
}
