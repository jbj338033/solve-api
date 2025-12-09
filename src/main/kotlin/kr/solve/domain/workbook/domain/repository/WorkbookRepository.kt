package kr.solve.domain.workbook.domain.repository

import kotlinx.coroutines.flow.Flow
import kr.solve.domain.workbook.domain.entity.Workbook
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import java.util.UUID

interface WorkbookRepository : CoroutineCrudRepository<Workbook, UUID> {
    @Query(
        """
        SELECT * FROM workbooks
        WHERE (:cursor IS NULL OR id < :cursor)
        ORDER BY id DESC
        LIMIT :limit
        """,
    )
    fun findAllByOrderByIdDesc(
        cursor: UUID?,
        limit: Int,
    ): Flow<Workbook>

    fun findAllByAuthorId(authorId: UUID): Flow<Workbook>
}
