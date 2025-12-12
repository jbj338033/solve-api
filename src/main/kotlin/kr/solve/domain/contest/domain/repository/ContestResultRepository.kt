package kr.solve.domain.contest.domain.repository

import kotlinx.coroutines.flow.Flow
import kr.solve.domain.contest.domain.entity.ContestResult
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface ContestResultRepository : CoroutineCrudRepository<ContestResult, Long> {
    fun findAllByContestIdAndUserId(
        contestId: Long,
        userId: Long,
    ): Flow<ContestResult>

    suspend fun findByContestIdAndUserIdAndProblemId(
        contestId: Long,
        userId: Long,
        problemId: Long,
    ): ContestResult?
}
