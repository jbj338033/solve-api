package kr.solve.domain.problem.domain.repository

import kotlinx.coroutines.flow.Flow
import kr.solve.domain.problem.domain.entity.Problem
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface ProblemRepository : CoroutineCrudRepository<Problem, Long> {
    @Query(
        """
        SELECT * FROM problems
        WHERE is_public = true AND (:cursor IS NULL OR id < :cursor)
        ORDER BY id DESC
        LIMIT :limit
        """,
    )
    fun findAllByIsPublicTrueOrderByIdDesc(
        cursor: Long?,
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
        cursor: Long?,
        limit: Int,
    ): Flow<Problem>

    fun findAllByAuthorId(authorId: Long): Flow<Problem>

    @Query("SELECT * FROM problems WHERE id IN (:ids)")
    fun findAllByIdIn(ids: List<Long>): Flow<Problem>
}
