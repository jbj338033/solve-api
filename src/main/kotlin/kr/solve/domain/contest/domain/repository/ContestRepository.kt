package kr.solve.domain.contest.domain.repository

import kotlinx.coroutines.flow.Flow
import kr.solve.domain.contest.domain.entity.Contest
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import java.util.UUID

interface ContestRepository : CoroutineCrudRepository<Contest, UUID> {
    @Query(
        """
        SELECT * FROM contests
        WHERE (:cursor IS NULL OR id < :cursor)
        ORDER BY id DESC
        LIMIT :limit
        """,
    )
    fun findAllByOrderByIdDesc(
        cursor: UUID?,
        limit: Int,
    ): Flow<Contest>

    fun findAllByHostId(hostId: UUID): Flow<Contest>

    fun findAllByIdIn(ids: List<UUID>): Flow<Contest>

    suspend fun findByInviteCode(inviteCode: String): Contest?
}
