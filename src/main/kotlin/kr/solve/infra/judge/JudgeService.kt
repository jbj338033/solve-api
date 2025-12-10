package kr.solve.infra.judge

import kotlinx.coroutines.flow.Flow
import kr.solve.infra.worker.WorkerClient
import org.springframework.stereotype.Service

@Service
class JudgeService(
    private val workerClient: WorkerClient,
) {
    fun judge(request: JudgeRequest): Flow<JudgeEvent> = workerClient.startJudge(request)
}
