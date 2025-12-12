package kr.solve.domain.workbook.domain.repository

import kotlinx.coroutines.flow.Flow
import kr.solve.domain.workbook.domain.entity.WorkbookProblem
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface WorkbookProblemRepository : CoroutineCrudRepository<WorkbookProblem, Long> {
    fun findAllByWorkbookIdOrderByOrder(workbookId: Long): Flow<WorkbookProblem>

    suspend fun findByWorkbookIdAndProblemId(
        workbookId: Long,
        problemId: Long,
    ): WorkbookProblem?

    suspend fun deleteAllByWorkbookId(workbookId: Long)
}
