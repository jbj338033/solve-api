package kr.solve.domain.problem.domain.repository

import kr.solve.domain.problem.domain.entity.ProblemStats
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import java.util.UUID

interface ProblemStatsRepository : CoroutineCrudRepository<ProblemStats, UUID> {
    suspend fun findByProblemId(problemId: UUID): ProblemStats?
}
