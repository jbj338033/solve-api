package kr.solve.domain.tag.presentation.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import kr.solve.domain.tag.application.service.AdminTagService
import kr.solve.domain.tag.presentation.request.AdminCreateTagRequest
import kr.solve.domain.tag.presentation.request.AdminUpdateTagRequest
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@Tag(name = "Admin - Tag", description = "태그 관리 API")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
@RestController
@RequestMapping("/admin/tags")
class AdminTagController(
    private val adminTagService: AdminTagService,
) {
    @Operation(summary = "태그 목록 조회")
    @GetMapping
    fun getTags() = adminTagService.getTags()

    @Operation(summary = "태그 생성")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    suspend fun createTag(
        @Valid @RequestBody request: AdminCreateTagRequest,
    ) = adminTagService.createTag(request.name)

    @Operation(summary = "태그 수정")
    @PatchMapping("/{tagId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    suspend fun updateTag(
        @PathVariable tagId: UUID,
        @Valid @RequestBody request: AdminUpdateTagRequest,
    ) = adminTagService.updateTag(tagId, request.name)

    @Operation(summary = "태그 삭제")
    @DeleteMapping("/{tagId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    suspend fun deleteTag(
        @PathVariable tagId: UUID,
    ) = adminTagService.deleteTag(tagId)
}
