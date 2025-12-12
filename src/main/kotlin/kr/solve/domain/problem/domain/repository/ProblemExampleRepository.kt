package kr.solve.domain.problem.domain.repository

import kotlinx.coroutines.flow.Flow
import kr.solve.domain.problem.domain.entity.ProblemExample
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface ProblemExampleRepository : CoroutineCrudRepository<ProblemExample, Long> {
    fun findAllByProblemIdOrderByOrder(problemId: Long): Flow<ProblemExample>

    suspend fun deleteAllByProblemId(problemId: Long)
}
