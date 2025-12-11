package kr.solve.domain.tag.presentation.response

import io.swagger.v3.oas.annotations.media.Schema
import kr.solve.domain.tag.domain.entity.Tag
import java.time.LocalDateTime
import java.util.UUID

fun Tag.toAdminResponse() =
    AdminTagResponse(
        id = id,
        name = name,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

@Schema(name = "Admin.Tag", description = "태그 상세 정보")
data class AdminTagResponse(
    @Schema(description = "태그 ID")
    val id: UUID,
    @Schema(description = "태그 이름", example = "DP")
    val name: String,
    @Schema(description = "생성일시")
    val createdAt: LocalDateTime?,
    @Schema(description = "수정일시")
    val updatedAt: LocalDateTime?,
)
