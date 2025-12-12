package kr.solve.domain.problem.domain.repository

import kotlinx.coroutines.flow.Flow
import kr.solve.domain.problem.domain.entity.Problem
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import java.util.UUID

interface ProblemRepository : CoroutineCrudRepository<Problem, UUID> {
    suspend fun findByNumber(number: Int): Problem?

    suspend fun existsByNumber(number: Int): Boolean
    @Query(
        """
        SELECT * FROM problems
        WHERE is_public = true AND (:cursor IS NULL OR id < :cursor)
        ORDER BY id DESC
        LIMIT :limit
        """,
    )
    fun findAllByIsPublicTrueOrderByIdDesc(
        cursor: UUID?,
        limit: Int,
    ): Flow<Problem>

    @Query(
        """
        SELECT * FROM problems
        WHERE (:cursor IS NULL OR id < :cursor)
        ORDER BY id DESC
        LIMIT :limit
        """,
    )
    fun findAllByOrderByIdDesc(
        cursor: UUID?,
        limit: Int,
    ): Flow<Problem>

    fun findAllByAuthorId(authorId: UUID): Flow<Problem>

    @Query("SELECT * FROM problems WHERE id IN (:ids)")
    fun findAllByIdIn(ids: List<UUID>): Flow<Problem>
}
