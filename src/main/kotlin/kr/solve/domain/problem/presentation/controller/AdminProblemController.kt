package kr.solve.domain.problem.presentation.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import kr.solve.domain.problem.application.service.AdminProblemService
import kr.solve.domain.problem.presentation.request.UpdateProblemRequest
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@Tag(name = "Admin - Problem", description = "문제 관리 API")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
@RestController
@RequestMapping("/admin/problems")
class AdminProblemController(
    private val adminProblemService: AdminProblemService,
) {
    @Operation(summary = "문제 목록 조회")
    @GetMapping
    suspend fun getProblems(
        @RequestParam(required = false) cursor: UUID?,
        @RequestParam(defaultValue = "20") limit: Int,
    ) = adminProblemService.getProblems(cursor, limit.coerceIn(1, 100))

    @Operation(summary = "문제 상세 조회")
    @GetMapping("/{problemId}")
    suspend fun getProblem(
        @PathVariable problemId: UUID,
    ) = adminProblemService.getProblem(problemId)

    @Operation(summary = "문제 수정")
    @PatchMapping("/{problemId}")
    suspend fun updateProblem(
        @PathVariable problemId: UUID,
        @Valid @RequestBody request: UpdateProblemRequest,
    ) = adminProblemService.updateProblem(problemId, request)

    @Operation(summary = "문제 삭제")
    @DeleteMapping("/{problemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    suspend fun deleteProblem(
        @PathVariable problemId: UUID,
    ) = adminProblemService.deleteProblem(problemId)
}
