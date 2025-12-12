package kr.solve.domain.workbook.domain.repository

import kotlinx.coroutines.flow.Flow
import kr.solve.domain.workbook.domain.entity.Workbook
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface WorkbookRepository : CoroutineCrudRepository<Workbook, Long> {
    @Query(
        """
        SELECT * FROM workbooks
        WHERE (:cursor IS NULL OR id < :cursor)
        ORDER BY id DESC
        LIMIT :limit
        """,
    )
    fun findAllByOrderByIdDesc(
        cursor: Long?,
        limit: Int,
    ): Flow<Workbook>

    fun findAllByAuthorId(authorId: Long): Flow<Workbook>
}
