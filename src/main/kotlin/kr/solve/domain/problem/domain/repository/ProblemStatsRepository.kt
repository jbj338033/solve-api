package kr.solve.domain.problem.domain.repository

import kr.solve.domain.problem.domain.entity.ProblemStats
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface ProblemStatsRepository : CoroutineCrudRepository<ProblemStats, Long> {
    suspend fun findByProblemId(problemId: Long): ProblemStats?
}
