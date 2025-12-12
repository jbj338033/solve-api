package kr.solve.domain.tag.presentation.response

import io.swagger.v3.oas.annotations.media.Schema
import kr.solve.domain.tag.domain.entity.Tag

fun Tag.toResponse() =
    TagResponse(
        id = id,
        name = name,
    )

@Schema(name = "Tag", description = "태그 정보")
data class TagResponse(
    @Schema(description = "태그 ID")
    val id: Long?,
    @Schema(description = "태그 이름", example = "DP")
    val name: String,
)
