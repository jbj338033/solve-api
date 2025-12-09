package kr.solve.domain.contest.domain.repository

import kotlinx.coroutines.flow.Flow
import kr.solve.domain.contest.domain.entity.ContestProblem
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import java.util.UUID

interface ContestProblemRepository : CoroutineCrudRepository<ContestProblem, UUID> {
    fun findAllByContestIdOrderByOrder(contestId: UUID): Flow<ContestProblem>

    suspend fun findByContestIdAndProblemId(
        contestId: UUID,
        problemId: UUID,
    ): ContestProblem?

    suspend fun deleteAllByContestId(contestId: UUID)
}
