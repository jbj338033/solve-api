package kr.solve.domain.contest.presentation.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import kr.solve.domain.contest.application.service.AdminContestService
import kr.solve.domain.contest.presentation.request.AdminCreateContestRequest
import kr.solve.domain.contest.presentation.request.AdminUpdateContestRequest
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
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

@Tag(name = "Admin - Contest", description = "대회 관리 API")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
@RestController
@RequestMapping("/admin/contests")
class AdminContestController(
    private val adminContestService: AdminContestService,
) {
    @Operation(summary = "대회 목록 조회")
    @GetMapping
    suspend fun getContests(
        @RequestParam(required = false) cursor: Long?,
        @RequestParam(defaultValue = "20") limit: Int,
    ) = adminContestService.getContests(cursor, limit.coerceIn(1, 100))

    @Operation(summary = "대회 상세 조회")
    @GetMapping("/{contestId}")
    suspend fun getContest(
        @PathVariable contestId: Long,
    ) = adminContestService.getContest(contestId)

    @Operation(summary = "대회 생성")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    suspend fun createContest(
        @Valid @RequestBody request: AdminCreateContestRequest,
    ) = adminContestService.createContest(request)

    @Operation(summary = "대회 수정")
    @PatchMapping("/{contestId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    suspend fun updateContest(
        @PathVariable contestId: Long,
        @Valid @RequestBody request: AdminUpdateContestRequest,
    ) = adminContestService.updateContest(contestId, request)

    @Operation(summary = "대회 삭제")
    @DeleteMapping("/{contestId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    suspend fun deleteContest(
        @PathVariable contestId: Long,
    ) = adminContestService.deleteContest(contestId)
}
