package kr.solve.domain.tag.presentation.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import kr.solve.domain.tag.application.service.TagService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Tag", description = "태그 API")
@RestController
@RequestMapping("/tags")
class TagController(
    private val tagService: TagService,
) {
    @Operation(summary = "태그 목록 조회")
    @GetMapping
    fun getTags() = tagService.getTags()
}
