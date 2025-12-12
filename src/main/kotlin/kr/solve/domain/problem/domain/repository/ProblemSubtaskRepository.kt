package kr.solve.domain.problem.domain.repository

import kotlinx.coroutines.flow.Flow
import kr.solve.domain.problem.domain.entity.ProblemSubtask
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface ProblemSubtaskRepository : CoroutineCrudRepository<ProblemSubtask, Long> {
    fun findAllByProblemIdOrderByOrder(problemId: Long): Flow<ProblemSubtask>

    suspend fun deleteAllByProblemId(problemId: Long)
}
