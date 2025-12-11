package kr.solve.domain.banner.presentation.response

import io.swagger.v3.oas.annotations.media.Schema
import kr.solve.domain.banner.domain.entity.Banner
import java.time.LocalDateTime
import java.util.UUID

fun Banner.toAdminResponse() = AdminBannerResponse(
    id = id,
    name = name,
    description = description,
    imageUrl = imageUrl,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

@Schema(name = "Admin.Banner", description = "배너 정보")
data class AdminBannerResponse(
    @Schema(description = "배너 ID")
    val id: UUID,
    @Schema(description = "배너 이름", example = "골드 달성")
    val name: String,
    @Schema(description = "배너 설명", example = "골드 티어 달성 시 획득")
    val description: String,
    @Schema(description = "배너 이미지 URL")
    val imageUrl: String,
    @Schema(description = "생성일시")
    val createdAt: LocalDateTime?,
    @Schema(description = "수정일시")
    val updatedAt: LocalDateTime?,
)
