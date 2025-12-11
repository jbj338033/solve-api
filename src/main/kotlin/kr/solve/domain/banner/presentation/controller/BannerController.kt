package kr.solve.domain.banner.presentation.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import kr.solve.domain.banner.application.service.BannerService
import kr.solve.domain.banner.presentation.request.SelectBannerRequest
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Banner", description = "배너 API")
@RestController
@RequestMapping("/banners")
class BannerController(
    private val bannerService: BannerService,
) {
    @Operation(summary = "전체 배너 목록")
    @GetMapping
    fun getBanners() = bannerService.getBanners()

    @Operation(summary = "내 배너 목록", security = [SecurityRequirement(name = "bearerAuth")])
    @GetMapping("/me")
    suspend fun getMyBanners() = bannerService.getMyBanners()

    @Operation(summary = "배너 선택", security = [SecurityRequirement(name = "bearerAuth")])
    @PutMapping("/me/selected")
    suspend fun selectBanner(
        @Valid @RequestBody request: SelectBannerRequest,
    ) = bannerService.selectBanner(request.id)
}
