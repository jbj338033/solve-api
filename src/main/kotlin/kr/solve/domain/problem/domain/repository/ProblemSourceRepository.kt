package kr.solve.domain.problem.domain.repository

import kr.solve.domain.problem.domain.entity.ProblemSource
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface ProblemSourceRepository : CoroutineCrudRepository<ProblemSource, Long> {
    suspend fun findByProblemId(problemId: Long): ProblemSource?
    suspend fun deleteByProblemId(problemId: Long)
}
