package kr.solve.domain.submission.presentation.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import kr.solve.domain.submission.application.service.SubmissionService
import kr.solve.domain.submission.presentation.request.CreateSubmissionRequest
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@Tag(name = "Contest", description = "대회 API")
@RestController
@RequestMapping("/contests")
class ContestSubmissionController(
    private val submissionService: SubmissionService,
) {
    @Operation(summary = "코드 제출", security = [SecurityRequirement(name = "bearerAuth")])
    @PostMapping("/{contestId}/problems/{problemNumber}/submissions")
    @ResponseStatus(HttpStatus.CREATED)
    suspend fun createContestSubmission(
        @PathVariable contestId: UUID,
        @PathVariable problemNumber: Int,
        @RequestBody @Valid request: CreateSubmissionRequest,
    ) = submissionService.createContestSubmission(contestId, problemNumber, request)
}
