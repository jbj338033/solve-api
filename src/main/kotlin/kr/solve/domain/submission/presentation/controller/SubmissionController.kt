package kr.solve.domain.submission.presentation.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import kr.solve.domain.submission.application.service.SubmissionService
import kr.solve.domain.submission.domain.enums.JudgeResult
import kr.solve.domain.submission.domain.enums.Language
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@Tag(name = "Submission", description = "제출 API")
@RestController
@RequestMapping("/submissions")
class SubmissionController(
    private val submissionService: SubmissionService,
) {
    @Operation(summary = "제출 목록 조회")
    @GetMapping
    suspend fun getSubmissions(
        @RequestParam(required = false) cursor: UUID?,
        @RequestParam(defaultValue = "20") limit: Int,
        @RequestParam(required = false) username: String?,
        @RequestParam(required = false) problemId: UUID?,
        @RequestParam(required = false) language: Language?,
        @RequestParam(required = false) result: JudgeResult?,
    ) = submissionService.getSubmissions(cursor, limit.coerceIn(1, 100), username, problemId, language, result)

    @Operation(summary = "제출 상세 조회")
    @GetMapping("/{submissionId}")
    suspend fun getSubmission(
        @PathVariable submissionId: UUID,
    ) = submissionService.getSubmission(submissionId)
}
