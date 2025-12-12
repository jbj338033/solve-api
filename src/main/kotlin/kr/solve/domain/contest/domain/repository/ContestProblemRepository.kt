package kr.solve.domain.contest.domain.repository

import kotlinx.coroutines.flow.Flow
import kr.solve.domain.contest.domain.entity.ContestProblem
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface ContestProblemRepository : CoroutineCrudRepository<ContestProblem, Long> {
    fun findAllByContestIdOrderByOrder(contestId: Long): Flow<ContestProblem>

    suspend fun findByContestIdAndProblemId(
        contestId: Long,
        problemId: Long,
    ): ContestProblem?

    suspend fun deleteAllByContestId(contestId: Long)
}
