package kr.solve.domain.problem.domain.repository

import kotlinx.coroutines.flow.Flow
import kr.solve.domain.problem.domain.entity.ProblemTestCase
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface ProblemTestCaseRepository : CoroutineCrudRepository<ProblemTestCase, Long> {
    fun findAllByProblemIdOrderByOrder(problemId: Long): Flow<ProblemTestCase>

    fun findAllBySubtaskId(subtaskId: Long): Flow<ProblemTestCase>

    suspend fun countByProblemId(problemId: Long): Int

    suspend fun deleteAllByProblemId(problemId: Long)
}
