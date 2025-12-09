package kr.solve.domain.problem.domain.repository

import kotlinx.coroutines.flow.Flow
import kr.solve.domain.problem.domain.entity.ProblemExample
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import java.util.UUID

interface ProblemExampleRepository : CoroutineCrudRepository<ProblemExample, UUID> {
    fun findAllByProblemIdOrderByOrder(problemId: UUID): Flow<ProblemExample>

    suspend fun deleteAllByProblemId(problemId: UUID)
}
