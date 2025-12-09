package kr.solve.domain.contest.domain.repository

import kotlinx.coroutines.flow.Flow
import kr.solve.domain.contest.domain.entity.ContestResult
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import java.util.UUID

interface ContestResultRepository : CoroutineCrudRepository<ContestResult, UUID> {
    fun findAllByContestIdAndUserId(
        contestId: UUID,
        userId: UUID,
    ): Flow<ContestResult>

    suspend fun findByContestIdAndUserIdAndProblemId(
        contestId: UUID,
        userId: UUID,
        problemId: UUID,
    ): ContestResult?
}
