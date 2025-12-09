package kr.solve.domain.problem.domain.repository

import kotlinx.coroutines.flow.Flow
import kr.solve.domain.problem.domain.entity.ProblemSubtask
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import java.util.UUID

interface ProblemSubtaskRepository : CoroutineCrudRepository<ProblemSubtask, UUID> {
    fun findAllByProblemIdOrderByOrder(problemId: UUID): Flow<ProblemSubtask>

    suspend fun deleteAllByProblemId(problemId: UUID)
}
