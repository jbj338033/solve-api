package kr.solve.domain.contest.domain.repository

import kotlinx.coroutines.flow.Flow
import kr.solve.domain.contest.domain.entity.Contest
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface ContestRepository : CoroutineCrudRepository<Contest, Long> {
    @Query(
        """
        SELECT * FROM contests
        WHERE (:cursor IS NULL OR id < :cursor)
        ORDER BY id DESC
        LIMIT :limit
        """,
    )
    fun findAllByOrderByIdDesc(
        cursor: Long?,
        limit: Int,
    ): Flow<Contest>

    fun findAllByHostId(hostId: Long): Flow<Contest>

    fun findAllByIdIn(ids: List<Long>): Flow<Contest>

    suspend fun findByInviteCode(inviteCode: String): Contest?
}
