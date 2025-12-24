package kr.solve.domain.problem.application.service

import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kr.solve.common.pagination.CursorPage
import kr.solve.domain.problem.domain.entity.Problem
import kr.solve.domain.problem.domain.entity.ProblemExample
import kr.solve.domain.problem.domain.entity.ProblemTestCase
import kr.solve.domain.problem.domain.error.ProblemError
import kr.solve.domain.user.domain.error.UserError
import kr.solve.domain.problem.domain.repository.ProblemExampleRepository
import kr.solve.domain.problem.domain.repository.ProblemRepository
import kr.solve.domain.problem.domain.repository.ProblemSourceRepository
import kr.solve.domain.problem.domain.repository.ProblemTagRepository
import kr.solve.domain.problem.domain.repository.ProblemTestCaseRepository
import kr.solve.domain.problem.presentation.response.ProblemSourceResponse
import kr.solve.domain.problem.presentation.request.AdminCreateProblemRequest
import kr.solve.domain.problem.presentation.request.AdminExampleRequest
import kr.solve.domain.problem.presentation.request.AdminTestCaseRequest
import kr.solve.domain.problem.presentation.request.AdminUpdateProblemRequest
import kr.solve.domain.problem.presentation.response.AdminProblemResponse
import kr.solve.domain.problem.presentation.response.toAdminDetail
import kr.solve.domain.problem.presentation.response.toAdminSummary
import kr.solve.domain.tag.domain.repository.TagRepository
import kr.solve.domain.user.domain.repository.UserRepository
import kr.solve.global.error.BusinessException
import kr.solve.global.security.userId
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AdminProblemService(
    private val problemRepository: ProblemRepository,
    private val problemExampleRepository: ProblemExampleRepository,
    private val problemTestCaseRepository: ProblemTestCaseRepository,
    private val problemSourceRepository: ProblemSourceRepository,
    private val problemTagRepository: ProblemTagRepository,
    private val tagRepository: TagRepository,
    private val userRepository: UserRepository,
) {
    suspend fun getProblems(
        cursor: Long?,
        limit: Int,
    ): CursorPage<AdminProblemResponse.Summary> {
        val problems = problemRepository.findAllByOrderByIdDesc(cursor, limit + 1).toList()
        val authorMap = userRepository.findAllByIdIn(problems.map { it.authorId }).toList().associateBy { it.id }

        return CursorPage.of(problems, limit) { problem ->
            val author = authorMap[problem.authorId] ?: return@of null
            problem.toAdminSummary(author)
        }
    }

    suspend fun getProblem(problemId: Long): AdminProblemResponse.Detail {
        val problem =
            problemRepository.findById(problemId)
                ?: throw BusinessException(ProblemError.NotFound)

        val author =
            userRepository.findById(problem.authorId)
                ?: throw BusinessException(UserError.NotFound)
        val examples = problemExampleRepository.findAllByProblemIdOrderByOrder(problem.id!!).toList()
        val testcases = problemTestCaseRepository.findAllByProblemIdOrderByOrder(problem.id!!).toList()
        val tags = getTagsByProblemId(problem.id!!)

        return problem.toAdminDetail(
            author,
            examples.map { AdminProblemResponse.Example(it.input, it.output, it.order) },
            testcases.map { AdminProblemResponse.TestCase(it.id!!, it.input, it.output, it.order) },
            tags,
        )
    }

    @Transactional
    suspend fun createProblem(request: AdminCreateProblemRequest) {
        val problem =
            problemRepository.save(
                Problem(
                    title = request.title,
                    description = request.description,
                    inputFormat = request.inputFormat,
                    outputFormat = request.outputFormat,
                    difficulty = request.difficulty,
                    timeLimit = request.timeLimit,
                    memoryLimit = request.memoryLimit,
                    authorId = userId(),
                    isPublic = request.isPublic,
                    type = request.type,
                    checkerCode = request.checkerCode,
                    checkerLanguage = request.checkerLanguage,
                    interactorCode = request.interactorCode,
                    interactorLanguage = request.interactorLanguage,
                ),
            )

        saveExamples(problem.id!!, request.examples)
        saveTestCases(problem.id!!, request.testcases)
        saveTags(problem.id!!, request.tagIds)
    }

    @Transactional
    suspend fun updateProblem(
        problemId: Long,
        request: AdminUpdateProblemRequest,
    ) {
        val problem =
            problemRepository.findById(problemId)
                ?: throw BusinessException(ProblemError.NotFound)

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

        request.testcases?.let {
            problemTestCaseRepository.deleteAllByProblemId(problemId)
            saveTestCases(problemId, it)
        }

        request.tagIds?.let {
            problemTagRepository.deleteAllByProblemId(problemId)
            saveTags(problemId, it)
        }
    }

    @Transactional
    suspend fun deleteProblem(problemId: Long) {
        val problem =
            problemRepository.findById(problemId)
                ?: throw BusinessException(ProblemError.NotFound)

        problemExampleRepository.deleteAllByProblemId(problemId)
        problemTestCaseRepository.deleteAllByProblemId(problemId)
        problemTagRepository.deleteAllByProblemId(problemId)
        problemRepository.delete(problem)
    }

    suspend fun getSource(problemId: Long): ProblemSourceResponse {
        problemRepository.findById(problemId)
            ?: throw BusinessException(ProblemError.NotFound)

        val source = problemSourceRepository.findByProblemId(problemId)
            ?: throw BusinessException(ProblemError.SolutionNotFound)

        return ProblemSourceResponse(
            solutionCode = source.solutionCode,
            solutionLanguage = source.solutionLanguage,
            generatorCode = source.generatorCode,
            generatorLanguage = source.generatorLanguage,
        )
    }

    private suspend fun saveExamples(
        problemId: Long,
        examples: List<AdminExampleRequest>,
    ) {
        examples.forEachIndexed { index, example ->
            problemExampleRepository.save(
                ProblemExample(problemId = problemId, input = example.input, output = example.output, order = index),
            )
        }
    }

    private suspend fun saveTestCases(
        problemId: Long,
        testcases: List<AdminTestCaseRequest>,
    ) {
        testcases.forEachIndexed { index, testcase ->
            problemTestCaseRepository.save(
                ProblemTestCase(problemId = problemId, input = testcase.input, output = testcase.output, order = index),
            )
        }
    }

    private suspend fun saveTags(
        problemId: Long,
        tagIds: List<Long>,
    ) {
        tagIds.forEach { tagId -> problemTagRepository.insert(problemId, tagId) }
    }

    private suspend fun getTagsByProblemId(problemId: Long): List<AdminProblemResponse.Tag> {
        val tagIds = problemTagRepository.findAllByProblemId(problemId).map { it.tagId }.toList()
        return tagRepository.findAllByIdIn(tagIds).toList().map { AdminProblemResponse.Tag(it.id!!, it.name) }
    }
}
