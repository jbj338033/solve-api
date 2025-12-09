package kr.solve.domain.workbook.presentation.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import kr.solve.domain.workbook.application.service.WorkbookService
import kr.solve.domain.workbook.presentation.request.CreateWorkbookRequest
import kr.solve.domain.workbook.presentation.request.UpdateWorkbookRequest
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

@Tag(name = "Workbook", description = "워크북 API")
@RestController
@RequestMapping("/workbooks")
class WorkbookController(
    private val workbookService: WorkbookService,
) {
    @Operation(summary = "워크북 목록 조회")
    @GetMapping
    suspend fun getWorkbooks(
        @RequestParam(required = false) cursor: UUID?,
        @RequestParam(defaultValue = "20") limit: Int,
    ) = workbookService.getWorkbooks(cursor, limit.coerceIn(1, 100))

    @Operation(summary = "워크북 상세 조회")
    @GetMapping("/{workbookId}")
    suspend fun getWorkbook(
        @PathVariable workbookId: UUID,
    ) = workbookService.getWorkbook(workbookId)

    @Operation(summary = "워크북 생성", security = [SecurityRequirement(name = "bearerAuth")])
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    suspend fun createWorkbook(
        @Valid @RequestBody request: CreateWorkbookRequest,
    ) = workbookService.createWorkbook(request)

    @Operation(summary = "워크북 수정", security = [SecurityRequirement(name = "bearerAuth")])
    @PatchMapping("/{workbookId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    suspend fun updateWorkbook(
        @PathVariable workbookId: UUID,
        @Valid @RequestBody request: UpdateWorkbookRequest,
    ) = workbookService.updateWorkbook(workbookId, request)

    @Operation(summary = "워크북 삭제", security = [SecurityRequirement(name = "bearerAuth")])
    @DeleteMapping("/{workbookId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    suspend fun deleteWorkbook(
        @PathVariable workbookId: UUID,
    ) = workbookService.deleteWorkbook(workbookId)
}
