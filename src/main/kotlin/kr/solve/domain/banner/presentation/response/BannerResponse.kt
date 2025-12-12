package kr.solve.domain.banner.presentation.response

import io.swagger.v3.oas.annotations.media.Schema
import kr.solve.domain.banner.domain.entity.Banner
import kr.solve.domain.banner.domain.entity.UserBanner
import java.time.LocalDateTime

fun Banner.toResponse() =
    BannerResponse.Summary(
        id = id,
        name = name,
        description = description,
        imageUrl = imageUrl,
    )

fun Banner.toAcquired(userBanner: UserBanner) =
    BannerResponse.Acquired(
        id = id,
        name = name,
        description = description,
        imageUrl = imageUrl,
        acquiredAt = userBanner.acquiredAt,
    )

object BannerResponse {
    @Schema(name = "Banner.Summary", description = "배너 요약 정보")
    data class Summary(
        @Schema(description = "배너 ID")
        val id: Long?,
        @Schema(description = "배너 이름", example = "골드 달성")
        val name: String,
        @Schema(description = "배너 설명", example = "골드 티어 달성 시 획득")
        val description: String,
        @Schema(description = "배너 이미지 URL")
        val imageUrl: String,
    )

    @Schema(name = "Banner.Acquired", description = "획득한 배너 정보")
    data class Acquired(
        @Schema(description = "배너 ID")
        val id: Long?,
        @Schema(description = "배너 이름", example = "골드 달성")
        val name: String,
        @Schema(description = "배너 설명", example = "골드 티어 달성 시 획득")
        val description: String,
        @Schema(description = "배너 이미지 URL")
        val imageUrl: String,
        @Schema(description = "획득일시")
        val acquiredAt: LocalDateTime,
    )
}
