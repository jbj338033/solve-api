package kr.solve.domain.problem.application.service

import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kr.solve.common.pagination.CursorPage
import kr.solve.domain.problem.domain.entity.ProblemExample
import kr.solve.domain.problem.domain.error.ProblemError
import kr.solve.domain.problem.domain.repository.ProblemExampleRepository
import kr.solve.domain.problem.domain.repository.ProblemRepository
import kr.solve.domain.problem.domain.repository.ProblemTagRepository
import kr.solve.domain.problem.presentation.request.ExampleRequest
import kr.solve.domain.problem.presentation.request.UpdateProblemRequest
import kr.solve.domain.problem.presentation.response.AdminProblemResponse
import kr.solve.domain.problem.presentation.response.toAdminDetail
import kr.solve.domain.problem.presentation.response.toAdminSummary
import kr.solve.domain.tag.domain.repository.TagRepository
import kr.solve.domain.user.domain.repository.UserRepository
import kr.solve.global.error.BusinessException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class AdminProblemService(
    private val problemRepository: ProblemRepository,
    private val problemExampleRepository: ProblemExampleRepository,
    private val problemTagRepository: ProblemTagRepository,
    private val tagRepository: TagRepository,
    private val userRepository: UserRepository,
) {
    suspend fun getProblems(
        cursor: UUID?,
        limit: Int,
    ): CursorPage<AdminProblemResponse.Summary> {
        val problems = problemRepository.findAllByOrderByIdDesc(cursor, limit + 1).toList()
        val authorMap = userRepository.findAllByIdIn(problems.map { it.authorId }).toList().associateBy { it.id }

        return CursorPage.of(problems, limit) { problem ->
            val author = authorMap[problem.authorId] ?: return@of null
            problem.toAdminSummary(author)
        }
    }

    suspend fun getProblem(problemId: UUID): AdminProblemResponse.Detail {
        val problem =
            problemRepository.findById(problemId)
                ?: throw BusinessException(ProblemError.NOT_FOUND)

        val author =
            userRepository.findById(problem.authorId)
                ?: throw BusinessException(ProblemError.AUTHOR_NOT_FOUND)
        val examples = problemExampleRepository.findAllByProblemIdOrderByOrder(problem.id).toList()
        val tags = getTagsByProblemId(problem.id)

        return problem.toAdminDetail(
            author,
            examples.map { AdminProblemResponse.Example(it.input, it.output, it.order) },
            tags,
        )
    }

    @Transactional
    suspend fun updateProblem(
        problemId: UUID,
        request: UpdateProblemRequest,
    ): AdminProblemResponse.Detail {
        val problem =
            problemRepository.findById(problemId)
                ?: throw BusinessException(ProblemError.NOT_FOUND)

        problemRepository.save(
            problem.copy(
                title = request.title ?: problem.title,
                description = request.description ?: problem.description,
                inputFormat = request.inputFormat ?: problem.inputFormat,
                outputFormat = request.outputFormat ?: problem.outputFormat,
                difficulty = request.difficulty ?: problem.difficulty,
                timeLimit = request.timeLimit ?: problem.timeLimit,
                memoryLimit = request.memoryLimit ?: problem.memoryLimit,
                isPublic = request.isPublic ?: problem.isPublic,
                type = request.type ?: problem.type,
                checkerCode = request.checkerCode ?: problem.checkerCode,
                checkerLanguage = request.checkerLanguage ?: problem.checkerLanguage,
                interactorCode = request.interactorCode ?: problem.interactorCode,
                interactorLanguage = request.interactorLanguage ?: problem.interactorLanguage,
            ),
        )

        request.examples?.let {
            problemExampleRepository.deleteAllByProblemId(problemId)
            saveExamples(problemId, it)
        }

        request.tagIds?.let {
            problemTagRepository.deleteAllByProblemId(problemId)
            saveTags(problemId, it)
        }

        return getProblem(problemId)
    }

    @Transactional
    suspend fun deleteProblem(problemId: UUID) {
        val problem =
            problemRepository.findById(problemId)
                ?: throw BusinessException(ProblemError.NOT_FOUND)

        problemExampleRepository.deleteAllByProblemId(problemId)
        problemTagRepository.deleteAllByProblemId(problemId)
        problemRepository.delete(problem)
    }

    private suspend fun saveExamples(
        problemId: UUID,
        examples: List<ExampleRequest>,
    ) {
        examples.forEachIndexed { index, example ->
            problemExampleRepository.save(
                ProblemExample(problemId = problemId, input = example.input, output = example.output, order = index),
            )
        }
    }

    private suspend fun saveTags(
        problemId: UUID,
        tagIds: List<UUID>,
    ) {
        tagIds.forEach { tagId -> problemTagRepository.insert(problemId, tagId) }
    }

    private suspend fun getTagsByProblemId(problemId: UUID): List<AdminProblemResponse.Tag> {
        val tagIds = problemTagRepository.findAllByProblemId(problemId).map { it.tagId }.toList()
        return tagRepository.findAllByIdIn(tagIds).toList().map { AdminProblemResponse.Tag(it.id, it.name) }
    }
}
