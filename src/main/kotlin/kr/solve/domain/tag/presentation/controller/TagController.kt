package kr.solve.domain.tag.presentation.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import kr.solve.domain.tag.application.service.TagService
import kr.solve.domain.tag.presentation.request.CreateTagRequest
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@Tag(name = "Tag", description = "태그 API")
@RestController
@RequestMapping("/tags")
class TagController(
    private val tagService: TagService,
) {
    @Operation(summary = "태그 목록 조회")
    @GetMapping
    fun getTags() = tagService.getTags()

    @Operation(summary = "태그 생성", description = "관리자 전용", security = [SecurityRequirement(name = "bearerAuth")])
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    suspend fun createTag(
        @Valid @RequestBody request: CreateTagRequest,
    ) {
        tagService.createTag(request)
    }

    @Operation(summary = "태그 삭제", description = "관리자 전용", security = [SecurityRequirement(name = "bearerAuth")])
    @DeleteMapping("/{tagId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    suspend fun deleteTag(
        @PathVariable tagId: UUID,
    ) = tagService.deleteTag(tagId)
}
