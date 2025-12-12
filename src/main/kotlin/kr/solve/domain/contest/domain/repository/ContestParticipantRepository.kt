package kr.solve.domain.contest.domain.repository

import kotlinx.coroutines.flow.Flow
import kr.solve.domain.contest.domain.entity.ContestParticipant
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface ContestParticipantRepository : CoroutineCrudRepository<ContestParticipant, Long> {
    fun findAllByContestIdOrderByTotalScoreDescPenaltyAsc(contestId: Long): Flow<ContestParticipant>

    fun findAllByUserId(userId: Long): Flow<ContestParticipant>

    suspend fun findByContestIdAndUserId(
        contestId: Long,
        userId: Long,
    ): ContestParticipant?

    suspend fun existsByContestIdAndUserId(
        contestId: Long,
        userId: Long,
    ): Boolean

    suspend fun countByContestId(contestId: Long): Long

    suspend fun deleteByContestIdAndUserId(
        contestId: Long,
        userId: Long,
    )

    suspend fun deleteAllByContestId(contestId: Long)
}
