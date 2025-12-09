package kr.solve.domain.problem.domain.repository

import kotlinx.coroutines.flow.Flow
import kr.solve.domain.problem.domain.entity.ProblemTestCase
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import java.util.UUID

interface ProblemTestCaseRepository : CoroutineCrudRepository<ProblemTestCase, UUID> {
    fun findAllByProblemIdOrderByOrder(problemId: UUID): Flow<ProblemTestCase>

    fun findAllBySubtaskId(subtaskId: UUID): Flow<ProblemTestCase>

    suspend fun countByProblemId(problemId: UUID): Int

    suspend fun deleteAllByProblemId(problemId: UUID)
}
