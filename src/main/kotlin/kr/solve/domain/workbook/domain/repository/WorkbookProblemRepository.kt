package kr.solve.domain.workbook.domain.repository

import kotlinx.coroutines.flow.Flow
import kr.solve.domain.workbook.domain.entity.WorkbookProblem
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import java.util.UUID

interface WorkbookProblemRepository : CoroutineCrudRepository<WorkbookProblem, UUID> {
    fun findAllByWorkbookIdOrderByOrder(workbookId: UUID): Flow<WorkbookProblem>

    suspend fun findByWorkbookIdAndProblemId(
        workbookId: UUID,
        problemId: UUID,
    ): WorkbookProblem?

    suspend fun deleteAllByWorkbookId(workbookId: UUID)
}
