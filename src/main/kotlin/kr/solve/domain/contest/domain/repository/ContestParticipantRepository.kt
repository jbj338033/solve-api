package kr.solve.domain.contest.domain.repository

import kotlinx.coroutines.flow.Flow
import kr.solve.domain.contest.domain.entity.ContestParticipant
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import java.util.UUID

interface ContestParticipantRepository : CoroutineCrudRepository<ContestParticipant, UUID> {
    fun findAllByContestIdOrderByTotalScoreDescPenaltyAsc(contestId: UUID): Flow<ContestParticipant>

    fun findAllByUserId(userId: UUID): Flow<ContestParticipant>

    suspend fun findByContestIdAndUserId(
        contestId: UUID,
        userId: UUID,
    ): ContestParticipant?

    suspend fun existsByContestIdAndUserId(
        contestId: UUID,
        userId: UUID,
    ): Boolean

    suspend fun deleteByContestIdAndUserId(
        contestId: UUID,
        userId: UUID,
    )
}
