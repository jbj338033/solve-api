package kr.solve.domain.problem.application.service

import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kr.solve.common.pagination.CursorPage
import kr.solve.domain.problem.domain.entity.Problem
import kr.solve.domain.problem.domain.entity.ProblemExample
import kr.solve.domain.problem.domain.enums.ProblemDifficulty
import kr.solve.domain.problem.domain.enums.ProblemSort
import kr.solve.domain.problem.domain.enums.ProblemStatus
import kr.solve.domain.problem.domain.enums.ProblemType
import kr.solve.domain.problem.domain.enums.SolveStatus
import kr.solve.domain.problem.domain.error.ProblemError
import kr.solve.domain.user.domain.error.UserError
import kr.solve.domain.problem.domain.repository.ProblemExampleRepository
import kr.solve.domain.problem.domain.repository.ProblemQueryRepository
import kr.solve.domain.problem.domain.repository.ProblemRepository
import kr.solve.domain.problem.domain.repository.ProblemSourceRepository
import kr.solve.domain.problem.domain.repository.ProblemTagRepository
import kr.solve.domain.problem.presentation.request.CreateProblemRequest
import kr.solve.domain.problem.presentation.request.ExampleRequest
import kr.solve.domain.problem.presentation.request.ProblemSourceRequest
import kr.solve.domain.problem.presentation.request.UpdateProblemRequest
import kr.solve.domain.problem.presentation.response.ProblemResponse
import kr.solve.domain.problem.presentation.response.ProblemSourceResponse
import kr.solve.domain.problem.presentation.response.toDetail
import kr.solve.domain.problem.presentation.response.toSummary
import kr.solve.domain.submission.domain.enums.JudgeResult
import kr.solve.domain.submission.domain.repository.SubmissionRepository
import kr.solve.domain.tag.domain.repository.TagRepository
import kr.solve.domain.user.domain.repository.UserRepository
import kr.solve.global.error.BusinessException
import kr.solve.global.security.userId
import kr.solve.global.security.userIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ProblemService(
    private val problemRepository: ProblemRepository,
    private val problemQueryRepository: ProblemQueryRepository,
    private val problemExampleRepository: ProblemExampleRepository,
    private val problemSourceRepository: ProblemSourceRepository,
    private val problemTagRepository: ProblemTagRepository,
    private val tagRepository: TagRepository,
    private val userRepository: UserRepository,
    private val submissionRepository: SubmissionRepository,
) {
    suspend fun getProblems(
        cursor: Long?,
        limit: Int,
        difficulties: List<ProblemDifficulty>?,
        type: ProblemType?,
        query: String?,
        tagIds: List<Long>?,
        sort: ProblemSort,
    ): CursorPage<ProblemResponse.Summary> {
        val problems =
            problemQueryRepository
                .findWithFilters(
                    cursor = cursor,
                    limit = limit + 1,
                    difficulties = difficulties,
                    type = type,
                    query = query?.takeIf { it.isNotBlank() },
                    tagIds = tagIds,
                    sort = sort,
                ).toList()

        val authorMap = userRepository.findAllByIdIn(problems.map { it.authorId }).toList().associateBy { it.id }
        val userId = userIdOrNull()
        val problemIds = problems.map { it.id!! }.toTypedArray()
        val solvedIds = userId?.let {
            submissionRepository.findSolvedProblemIdsByUserIdAndProblemIds(it, problemIds).toList().toSet()
        }
        val attemptedIds = userId?.let {
            submissionRepository.findAttemptedProblemIdsByUserIdAndProblemIds(it, problemIds).toList().toSet()
        }

        return CursorPage.of(problems, limit) { problem ->
            val author = authorMap[problem.authorId] ?: return@of null
            val status = userId?.let {
                when {
                    solvedIds?.contains(problem.id) == true -> SolveStatus.SOLVED
                    attemptedIds?.contains(problem.id) == true -> SolveStatus.ATTEMPTED
                    else -> null
                }
            }
            problem.toSummary(author, status)
        }
    }

    suspend fun getProblem(problemId: Long): ProblemResponse.Detail {
        val problem = problemRepository.findById(problemId)
            ?: throw BusinessException(ProblemError.NotFound)

        if (problem.status != ProblemStatus.APPROVED || !problem.isPublic) {
            throw BusinessException(ProblemError.AccessDenied)
        }

        return getProblemDetail(problem)
    }

    suspend fun getMyProblem(problemId: Long): ProblemResponse.Detail {
        val problem = problemRepository.findById(problemId)
            ?: throw BusinessException(ProblemError.NotFound)

        if (problem.authorId != userId()) {
            throw BusinessException(ProblemError.AccessDenied)
        }

        return getProblemDetail(problem)
    }

    suspend fun getMyProblems(): List<ProblemResponse.Summary> {
        val problems = problemRepository.findAllByAuthorId(userId()).toList()
        val author = userRepository.findById(userId())
            ?: throw BusinessException(UserError.NotFound)

        return problems.map { it.toSummary(author) }
    }

    private suspend fun getProblemDetail(problem: Problem): ProblemResponse.Detail {
        val author =
            userRepository.findById(problem.authorId)
                ?: throw BusinessException(UserError.NotFound)
        val examples = problemExampleRepository.findAllByProblemIdOrderByOrder(problem.id!!).toList()
        val tags = getTagsByProblemId(problem.id!!)
        val status = userIdOrNull()?.let { userId ->
            when {
                submissionRepository.existsByUserIdAndProblemIdAndResult(userId, problem.id!!, JudgeResult.ACCEPTED) -> SolveStatus.SOLVED
                submissionRepository.existsByUserIdAndProblemId(userId, problem.id!!) -> SolveStatus.ATTEMPTED
                else -> null
            }
        }

        return problem.toDetail(
            author,
            examples.map { ProblemResponse.Example(it.input, it.output, it.order) },
            tags,
            status,
        )
    }

    @Transactional
    suspend fun createProblem(request: CreateProblemRequest): ProblemResponse.Id {
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
        saveTags(problem.id!!, request.tagIds)

        return ProblemResponse.Id(problem.id!!)
    }

    @Transactional
    suspend fun updateProblem(
        problemId: Long,
        request: UpdateProblemRequest,
    ) {
        val problem = problemRepository.findById(problemId)
            ?: throw BusinessException(ProblemError.NotFound)
        validateOwner(problem)
        validateEditable(problem)

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
            problemExampleRepository.deleteAllByProblemId(problem.id!!)
            saveExamples(problem.id!!, it)
        }

        request.tagIds?.let {
            problemTagRepository.deleteAllByProblemId(problem.id!!)
            saveTags(problem.id!!, it)
        }
    }

    @Transactional
    suspend fun deleteProblem(problemId: Long) {
        val problem = problemRepository.findById(problemId)
            ?: throw BusinessException(ProblemError.NotFound)
        validateOwner(problem)
        validateDeletable(problem)
        problemExampleRepository.deleteAllByProblemId(problem.id!!)
        problemTagRepository.deleteAllByProblemId(problem.id!!)
        problemRepository.delete(problem)
    }

    suspend fun getSource(problemId: Long): ProblemSourceResponse {
        val problem = problemRepository.findById(problemId)
            ?: throw BusinessException(ProblemError.NotFound)
        validateOwner(problem)

        val source = problemSourceRepository.findByProblemId(problemId)
            ?: throw BusinessException(ProblemError.SolutionNotFound)

        return ProblemSourceResponse(
            solutionCode = source.solutionCode,
            solutionLanguage = source.solutionLanguage,
            generatorCode = source.generatorCode,
            generatorLanguage = source.generatorLanguage,
        )
    }

    @Transactional
    suspend fun saveSource(problemId: Long, request: ProblemSourceRequest) {
        val problem = problemRepository.findById(problemId)
            ?: throw BusinessException(ProblemError.NotFound)
        validateOwner(problem)

        problemSourceRepository.upsert(
            problemId = problemId,
            solutionCode = request.solutionCode,
            solutionLanguage = request.solutionLanguage,
            generatorCode = request.generatorCode,
            generatorLanguage = request.generatorLanguage,
        )
    }

    @Transactional
    suspend fun deleteSource(problemId: Long) {
        val problem = problemRepository.findById(problemId)
            ?: throw BusinessException(ProblemError.NotFound)
        validateOwner(problem)

        problemSourceRepository.deleteByProblemId(problemId)
    }

    private suspend fun validateOwner(problem: Problem) {
        if (problem.authorId != userId()) throw BusinessException(ProblemError.AccessDenied)
    }

    private fun validateEditable(problem: Problem) {
        if (problem.status != ProblemStatus.DRAFT && problem.status != ProblemStatus.REJECTED) {
            throw BusinessException(ProblemError.CannotEdit)
        }
    }

    private fun validateDeletable(problem: Problem) {
        if (problem.status != ProblemStatus.DRAFT && problem.status != ProblemStatus.REJECTED) {
            throw BusinessException(ProblemError.CannotDelete)
        }
    }

    private suspend fun saveExamples(
        problemId: Long,
        examples: List<ExampleRequest>,
    ) {
        examples.forEachIndexed { index, example ->
            problemExampleRepository.save(
                ProblemExample(problemId = problemId, input = example.input, output = example.output, order = index),
            )
        }
    }

    private suspend fun saveTags(
        problemId: Long,
        tagIds: List<Long>,
    ) {
        tagIds.forEach { tagId -> problemTagRepository.insert(problemId, tagId) }
    }

    private suspend fun getTagsByProblemId(problemId: Long): List<ProblemResponse.Tag> {
        val tagIds = problemTagRepository.findAllByProblemId(problemId).map { it.tagId }.toList()
        return tagRepository
            .findAllByIdIn(tagIds)
            .toList()
            .map { ProblemResponse.Tag(it.id!!, it.name) }
    }
}
