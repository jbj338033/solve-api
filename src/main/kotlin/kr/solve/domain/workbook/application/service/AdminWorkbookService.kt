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
import kr.solve.domain.workbook.presentation.request.AdminCreateWorkbookRequest
import kr.solve.domain.workbook.presentation.request.AdminUpdateWorkbookRequest
import kr.solve.domain.workbook.presentation.response.AdminWorkbookResponse
import kr.solve.domain.workbook.presentation.response.toAdminDetail
import kr.solve.domain.workbook.presentation.response.toAdminSummary
import kr.solve.global.error.BusinessException
import kr.solve.global.security.userId
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class AdminWorkbookService(
    private val workbookRepository: WorkbookRepository,
    private val workbookProblemRepository: WorkbookProblemRepository,
    private val problemRepository: ProblemRepository,
    private val userRepository: UserRepository,
) {
    suspend fun getWorkbooks(
        cursor: UUID?,
        limit: Int,
    ): CursorPage<AdminWorkbookResponse.Summary> {
        val workbooks = workbookRepository.findAllByOrderByIdDesc(cursor, limit + 1).toList()
        val authorMap = userRepository.findAllByIdIn(workbooks.map { it.authorId }).toList().associateBy { it.id }

        return CursorPage.of(workbooks, limit) { workbook ->
            val author = authorMap[workbook.authorId] ?: return@of null
            workbook.toAdminSummary(author)
        }
    }

    suspend fun getWorkbook(workbookId: UUID): AdminWorkbookResponse.Detail {
        val workbook =
            workbookRepository.findById(workbookId)
                ?: throw BusinessException(WorkbookError.NOT_FOUND)

        val author =
            userRepository.findById(workbook.authorId)
                ?: throw BusinessException(WorkbookError.AUTHOR_NOT_FOUND)
        val workbookProblems = workbookProblemRepository.findAllByWorkbookIdOrderByOrder(workbookId).toList()
        val problemMap = problemRepository.findAllByIdIn(workbookProblems.map { it.problemId }).toList().associateBy { it.id }
        val problems =
            workbookProblems.mapNotNull { wp ->
                problemMap[wp.problemId]?.let {
                    AdminWorkbookResponse.Problem(it.id, it.number, it.title, it.difficulty, it.type)
                }
            }

        return workbook.toAdminDetail(author, problems)
    }

    @Transactional
    suspend fun createWorkbook(request: AdminCreateWorkbookRequest) {
        val workbook =
            workbookRepository.save(
                Workbook(
                    title = request.title,
                    description = request.description,
                    authorId = userId(),
                ),
            )

        request.problemIds.forEachIndexed { index, problemId ->
            workbookProblemRepository.save(WorkbookProblem(workbookId = workbook.id, problemId = problemId, order = index))
        }
    }

    @Transactional
    suspend fun updateWorkbook(
        workbookId: UUID,
        request: AdminUpdateWorkbookRequest,
    ) {
        val workbook =
            workbookRepository.findById(workbookId)
                ?: throw BusinessException(WorkbookError.NOT_FOUND)

        workbookRepository.save(
            workbook.copy(
                title = request.title ?: workbook.title,
                description = request.description ?: workbook.description,
            ),
        )

        if (request.problemIds != null) {
            workbookProblemRepository.deleteAllByWorkbookId(workbookId)
            request.problemIds.forEachIndexed { index, problemId ->
                workbookProblemRepository.save(WorkbookProblem(workbookId = workbookId, problemId = problemId, order = index))
            }
        }
    }

    @Transactional
    suspend fun deleteWorkbook(workbookId: UUID) {
        val workbook =
            workbookRepository.findById(workbookId)
                ?: throw BusinessException(WorkbookError.NOT_FOUND)

        workbookProblemRepository.deleteAllByWorkbookId(workbookId)
        workbookRepository.delete(workbook)
    }
}
