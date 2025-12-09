package kr.solve.domain.contest.presentation.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import kr.solve.domain.contest.application.service.ContestService
import kr.solve.domain.contest.presentation.request.CreateContestRequest
import kr.solve.domain.contest.presentation.request.JoinContestRequest
import kr.solve.domain.contest.presentation.request.UpdateContestRequest
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

@Tag(name = "Contest", description = "대회 API")
@RestController
@RequestMapping("/contests")
class ContestController(
    private val contestService: ContestService,
) {
    @Operation(summary = "대회 목록 조회")
    @GetMapping
    suspend fun getContests(
        @RequestParam(required = false) cursor: UUID?,
        @RequestParam(defaultValue = "20") limit: Int,
    ) = contestService.getContests(cursor, limit.coerceIn(1, 100))

    @Operation(summary = "대회 상세 조회")
    @GetMapping("/{contestId}")
    suspend fun getContest(
        @PathVariable contestId: UUID,
    ) = contestService.getContest(contestId)

    @Operation(summary = "대회 생성", security = [SecurityRequirement(name = "bearerAuth")])
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    suspend fun createContest(
        @Valid @RequestBody request: CreateContestRequest,
    ) = contestService.createContest(request)

    @Operation(summary = "대회 수정", security = [SecurityRequirement(name = "bearerAuth")])
    @PatchMapping("/{contestId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    suspend fun updateContest(
        @PathVariable contestId: UUID,
        @Valid @RequestBody request: UpdateContestRequest,
    ) = contestService.updateContest(contestId, request)

    @Operation(summary = "대회 삭제", security = [SecurityRequirement(name = "bearerAuth")])
    @DeleteMapping("/{contestId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    suspend fun deleteContest(
        @PathVariable contestId: UUID,
    ) = contestService.deleteContest(contestId)

    @Operation(summary = "대회 참가", security = [SecurityRequirement(name = "bearerAuth")])
    @PostMapping("/{contestId}/join")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    suspend fun joinContest(
        @PathVariable contestId: UUID,
        @RequestBody request: JoinContestRequest,
    ) = contestService.joinContest(contestId, request)

    @Operation(summary = "대회 참가 취소", security = [SecurityRequirement(name = "bearerAuth")])
    @PostMapping("/{contestId}/leave")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    suspend fun leaveContest(
        @PathVariable contestId: UUID,
    ) = contestService.leaveContest(contestId)
}
