package kr.solve.domain.execution.application.service

import kotlinx.coroutines.flow.Flow
import kr.solve.domain.execution.domain.error.ExecutionError
import kr.solve.domain.problem.domain.repository.ProblemRepository
import kr.solve.domain.submission.domain.enums.Language
import kr.solve.global.error.BusinessException
import kr.solve.infra.worker.ExecuteCommand
import kr.solve.infra.worker.ExecuteEvent
import kr.solve.infra.worker.ExecuteRequest
import kr.solve.infra.worker.WorkerClient
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class ExecutionService(
    private val problemRepository: ProblemRepository,
    private val workerClient: WorkerClient,
) {
    companion object {
        private const val DEFAULT_TIME_LIMIT = 10000
    }

    suspend fun startExecution(
        problemId: UUID,
        language: Language,
        code: String,
    ): Pair<UUID, Flow<ExecuteEvent>> {
        val problem = problemRepository.findById(problemId)
            ?: throw BusinessException(ExecutionError.EXECUTION_UNAVAILABLE)

        val executionId = UUID.randomUUID()
        val request = ExecuteRequest(
            executionId = executionId,
            language = language,
            code = code,
            timeLimit = DEFAULT_TIME_LIMIT,
            memoryLimit = problem.memoryLimit,
        )

        val events = workerClient.startExecution(request)
        return executionId to events
    }

    suspend fun sendStdin(executionId: UUID, data: String) {
        workerClient.sendExecuteCommand(executionId.toString(), ExecuteCommand.Stdin(data))
    }

    suspend fun killExecution(executionId: UUID) {
        workerClient.sendExecuteCommand(executionId.toString(), ExecuteCommand.Kill)
    }
}
