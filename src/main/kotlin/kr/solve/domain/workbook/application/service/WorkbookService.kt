package kr.solve.domain.workbook.application.service

import kotlinx.coroutines.flow.toList
import kr.solve.common.pagination.CursorPage
import kr.solve.domain.problem.domain.repository.ProblemRepository
import kr.solve.domain.user.domain.repository.UserRepository
import kr.solve.domain.workbook.domain.entity.Workbook
import kr.solve.domain.workbook.domain.entity.WorkbookProblem
import kr.solve.domain.workbook.domain.error.WorkbookError
import kr.solve.domain.workbook.domain.repository.WorkbookProblemRepository
import kr.solve.domain.workbook.domain.repository.WorkbookRepository
import kr.solve.domain.workbook.presentation.request.CreateWorkbookRequest
import kr.solve.domain.workbook.presentation.request.UpdateWorkbookRequest
import kr.solve.domain.workbook.presentation.response.WorkbookResponse
import kr.solve.domain.workbook.presentation.response.toDetail
import kr.solve.domain.workbook.presentation.response.toSummary
import kr.solve.global.error.BusinessException
import kr.solve.global.security.userId
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class WorkbookService(
    private val workbookRepository: WorkbookRepository,
    private val workbookProblemRepository: WorkbookProblemRepository,
    private val problemRepository: ProblemRepository,
    private val userRepository: UserRepository,
) {
    suspend fun getWorkbooks(
        cursor: Long?,
        limit: Int,
    ): CursorPage<WorkbookResponse.Summary> {
        val workbooks = workbookRepository.findAllByOrderByIdDesc(cursor, limit + 1).toList()
        val authorMap = userRepository.findAllByIdIn(workbooks.map { it.authorId }).toList().associateBy { it.id }

        return CursorPage.of(workbooks, limit) { workbook ->
            val author = authorMap[workbook.authorId] ?: return@of null
            workbook.toSummary(author)
        }
    }

    suspend fun getWorkbook(workbookId: Long): WorkbookResponse.Detail {
        val workbook =
            workbookRepository.findById(workbookId)
                ?: throw BusinessException(WorkbookError.NOT_FOUND)

        val author =
            userRepository.findById(workbook.authorId)
                ?: throw BusinessException(WorkbookError.AUTHOR_NOT_FOUND)
        val workbookProblems = workbookProblemRepository.findAllByWorkbookIdOrderByOrder(workbookId).toList()
        val problemMap = problemRepository.findAllByIdIn(workbookProblems.map { it.problemId }).toList().associateBy { it.id!! }
        val problems =
            workbookProblems.mapNotNull { wp ->
                problemMap[wp.problemId]?.let {
                    WorkbookResponse.Problem(it.id!!, it.title, it.difficulty, it.type)
                }
            }

        return workbook.toDetail(author, problems)
    }

    @Transactional
    suspend fun createWorkbook(request: CreateWorkbookRequest) {
        val workbook =
            workbookRepository.save(
                Workbook(title = request.title, description = request.description, authorId = userId()),
            )

        request.problemIds.forEachIndexed { index, problemId ->
            workbookProblemRepository.save(WorkbookProblem(workbookId = workbook.id!!, problemId = problemId, order = index))
        }
    }

    @Transactional
    suspend fun updateWorkbook(
        workbookId: Long,
        request: UpdateWorkbookRequest,
    ) {
        val workbook =
            workbookRepository.findById(workbookId)
                ?: throw BusinessException(WorkbookError.NOT_FOUND)

        validateOwner(workbook)

        workbookRepository.save(
            workbook.copy(title = request.title ?: workbook.title, description = request.description ?: workbook.description),
        )

        if (request.problemIds != null) {
            workbookProblemRepository.deleteAllByWorkbookId(workbookId)

            request.problemIds.forEachIndexed { index, problemId ->
                workbookProblemRepository.save(WorkbookProblem(workbookId = workbookId, problemId = problemId, order = index))
            }
        }
    }

    @Transactional
    suspend fun deleteWorkbook(workbookId: Long) {
        val workbook =
            workbookRepository.findById(workbookId)
                ?: throw BusinessException(WorkbookError.NOT_FOUND)

        validateOwner(workbook)

        workbookProblemRepository.deleteAllByWorkbookId(workbookId)
        workbookRepository.delete(workbook)
    }

    private suspend fun validateOwner(workbook: Workbook) {
        if (workbook.authorId != userId()) throw BusinessException(WorkbookError.ACCESS_DENIED)
    }
}
