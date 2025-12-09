package kr.solve.domain.execution.application.service

import kotlinx.coroutines.channels.ReceiveChannel
import kr.solve.domain.execution.domain.error.ExecutionError
import kr.solve.domain.problem.domain.repository.ProblemRepository
import kr.solve.domain.submission.domain.enums.Language
import kr.solve.global.error.BusinessException
import kr.solve.infra.isolate.IsolateExecutor
import kr.solve.infra.isolate.IsolateResult
import org.springframework.stereotype.Service
import java.util.UUID

private const val INTERACTIVE_TIME_LIMIT = 60000

@Service
class ExecutionService(
    private val problemRepository: ProblemRepository,
    private val isolateExecutor: IsolateExecutor,
) {
    suspend fun execute(
        problemId: UUID,
        language: Language,
        code: String,
        inputChannel: ReceiveChannel<String>,
        onOutput: suspend (String) -> Unit,
        onError: suspend (String) -> Unit,
        onComplete: suspend (IsolateResult) -> Unit,
    ) {
        val problem =
            problemRepository.findById(problemId)
                ?: throw BusinessException(ExecutionError.EXECUTION_UNAVAILABLE)

        isolateExecutor.executeInteractive(
            language = language,
            code = code,
            timeLimit = INTERACTIVE_TIME_LIMIT,
            memoryLimit = problem.memoryLimit,
            inputChannel = inputChannel,
            onOutput = onOutput,
            onError = onError,
            onComplete = onComplete,
        )
    }
}
