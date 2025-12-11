package kr.solve.domain.problem.presentation.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import kr.solve.domain.problem.application.service.ProblemService
import kr.solve.domain.problem.domain.enums.ProblemDifficulty
import kr.solve.domain.problem.domain.enums.ProblemSort
import kr.solve.domain.problem.domain.enums.ProblemType
import kr.solve.domain.problem.presentation.request.CreateProblemRequest
import kr.solve.domain.problem.presentation.request.UpdateProblemRequest
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@Tag(name = "Problem", description = "문제 API")
@RestController
@RequestMapping("/problems")
class ProblemController(
    private val problemService: ProblemService,
) {
    @Operation(summary = "문제 목록 조회")
    @GetMapping
    suspend fun getProblems(
        @RequestParam(required = false) cursor: UUID?,
        @RequestParam(defaultValue = "20") limit: Int,
        @RequestParam(required = false) difficulties: List<ProblemDifficulty>?,
        @RequestParam(required = false) type: ProblemType?,
        @RequestParam(required = false) query: String?,
        @RequestParam(required = false) tagIds: List<UUID>?,
        @RequestParam(defaultValue = "LATEST") sort: ProblemSort,
    ) = problemService.getProblems(
        cursor = cursor,
        limit = limit.coerceIn(1, 100),
        difficulties = difficulties,
        type = type,
        query = query,
        tagIds = tagIds,
        sort = sort,
    )

    @Operation(summary = "문제 상세 조회")
    @GetMapping("/{problemId}")
    suspend fun getProblem(
        @PathVariable problemId: UUID,
    ) = problemService.getProblem(problemId)

    @Operation(summary = "문제 생성", security = [SecurityRequirement(name = "bearerAuth")])
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    suspend fun createProblem(
        @Valid @RequestBody request: CreateProblemRequest,
    ) = problemService.createProblem(request)

    @Operation(summary = "문제 수정", security = [SecurityRequirement(name = "bearerAuth")])
    @PatchMapping("/{problemId}")
    suspend fun updateProblem(
        @PathVariable problemId: UUID,
        @Valid @RequestBody request: UpdateProblemRequest,
    ) = problemService.updateProblem(problemId, request)

    @Operation(summary = "문제 삭제", security = [SecurityRequirement(name = "bearerAuth")])
    @DeleteMapping("/{problemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    suspend fun deleteProblem(
        @PathVariable problemId: UUID,
    ) = problemService.deleteProblem(problemId)
}
