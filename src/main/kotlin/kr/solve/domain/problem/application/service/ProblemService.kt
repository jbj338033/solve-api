package kr.solve.domain.problem.application.service

import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kr.solve.common.pagination.CursorPage
import kr.solve.domain.problem.domain.entity.Problem
import kr.solve.domain.problem.domain.entity.ProblemExample
import kr.solve.domain.problem.domain.enums.ProblemDifficulty
import kr.solve.domain.problem.domain.enums.ProblemSort
import kr.solve.domain.problem.domain.enums.ProblemType
import kr.solve.domain.problem.domain.enums.SolveStatus
import kr.solve.domain.problem.domain.error.ProblemError
import kr.solve.domain.problem.domain.repository.ProblemExampleRepository
import kr.solve.domain.problem.domain.repository.ProblemQueryRepository
import kr.solve.domain.problem.domain.repository.ProblemRepository
import kr.solve.domain.problem.domain.repository.ProblemTagRepository
import kr.solve.domain.problem.presentation.request.CreateProblemRequest
import kr.solve.domain.problem.presentation.request.ExampleRequest
import kr.solve.domain.problem.presentation.request.UpdateProblemRequest
import kr.solve.domain.problem.presentation.response.ProblemResponse
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
import java.util.UUID

@Service
class ProblemService(
    private val problemRepository: ProblemRepository,
    private val problemQueryRepository: ProblemQueryRepository,
    private val problemExampleRepository: ProblemExampleRepository,
    private val problemTagRepository: ProblemTagRepository,
    private val tagRepository: TagRepository,
    private val userRepository: UserRepository,
    private val submissionRepository: SubmissionRepository,
) {
    suspend fun getProblems(
        cursor: UUID?,
        limit: Int,
        difficulties: List<ProblemDifficulty>?,
        type: ProblemType?,
        query: String?,
        tagIds: List<UUID>?,
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
        val problemIds = problems.map { it.id }.toTypedArray()
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

    suspend fun getProblem(problemNumber: Int): ProblemResponse.Detail {
        val problem = findByNumber(problemNumber)
        if (!problem.isPublic) {
            throw BusinessException(ProblemError.ACCESS_DENIED)
        }

        return getProblemDetail(problem)
    }

    private suspend fun getProblemDetail(problem: Problem): ProblemResponse.Detail {
        val author =
            userRepository.findById(problem.authorId)
                ?: throw BusinessException(ProblemError.AUTHOR_NOT_FOUND)
        val examples = problemExampleRepository.findAllByProblemIdOrderByOrder(problem.id).toList()
        val tags = getTagsByProblemId(problem.id)
        val status = userIdOrNull()?.let { userId ->
            when {
                submissionRepository.existsByUserIdAndProblemIdAndResult(userId, problem.id, JudgeResult.ACCEPTED) -> SolveStatus.SOLVED
                submissionRepository.existsByUserIdAndProblemId(userId, problem.id) -> SolveStatus.ATTEMPTED
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
    suspend fun createProblem(request: CreateProblemRequest): ProblemResponse.Detail {
        if (problemRepository.existsByNumber(request.number)) {
            throw BusinessException(ProblemError.NUMBER_ALREADY_EXISTS)
        }

        val problem =
            problemRepository.save(
                Problem(
                    number = request.number,
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

        saveExamples(problem.id, request.examples)
        saveTags(problem.id, request.tagIds)

        val author =
            userRepository.findById(problem.authorId)
                ?: throw BusinessException(ProblemError.AUTHOR_NOT_FOUND)
        val examples = problemExampleRepository.findAllByProblemIdOrderByOrder(problem.id).toList()
        val tags = getTagsByProblemId(problem.id)

        return problem.toDetail(
            author,
            examples.map { ProblemResponse.Example(it.input, it.output, it.order) },
            tags,
        )
    }

    @Transactional
    suspend fun updateProblem(
        problemNumber: Int,
        request: UpdateProblemRequest,
    ): ProblemResponse.Detail {
        val problem = findByNumber(problemNumber)
        validateOwner(problem)

        if (request.number != null && request.number != problem.number) {
            if (problemRepository.existsByNumber(request.number)) {
                throw BusinessException(ProblemError.NUMBER_ALREADY_EXISTS)
            }
        }

        val updated =
            problemRepository.save(
                problem.copy(
                    number = request.number ?: problem.number,
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
            problemExampleRepository.deleteAllByProblemId(problem.id)
            saveExamples(problem.id, it)
        }

        request.tagIds?.let {
            problemTagRepository.deleteAllByProblemId(problem.id)
            saveTags(problem.id, it)
        }

        val author =
            userRepository.findById(updated.authorId)
                ?: throw BusinessException(ProblemError.AUTHOR_NOT_FOUND)
        val examples = problemExampleRepository.findAllByProblemIdOrderByOrder(updated.id).toList()
        val tags = getTagsByProblemId(updated.id)

        return updated.toDetail(
            author,
            examples.map { ProblemResponse.Example(it.input, it.output, it.order) },
            tags,
        )
    }

    @Transactional
    suspend fun deleteProblem(problemNumber: Int) {
        val problem = findByNumber(problemNumber)
        validateOwner(problem)
        problemExampleRepository.deleteAllByProblemId(problem.id)
        problemTagRepository.deleteAllByProblemId(problem.id)
        problemRepository.delete(problem)
    }

    private suspend fun findByNumber(problemNumber: Int): Problem =
        problemRepository.findByNumber(problemNumber) ?: throw BusinessException(ProblemError.NOT_FOUND)

    private suspend fun validateOwner(problem: Problem) {
        if (problem.authorId != userId()) throw BusinessException(ProblemError.ACCESS_DENIED)
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

    private suspend fun getTagsByProblemId(problemId: UUID): List<ProblemResponse.Tag> {
        val tagIds = problemTagRepository.findAllByProblemId(problemId).map { it.tagId }.toList()
        return tagRepository
            .findAllByIdIn(tagIds)
            .toList()
            .map { ProblemResponse.Tag(it.id, it.name) }
    }
}
