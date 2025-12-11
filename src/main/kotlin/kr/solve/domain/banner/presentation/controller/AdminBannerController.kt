package kr.solve.domain.banner.presentation.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import kr.solve.domain.banner.application.service.AdminBannerService
import kr.solve.domain.banner.presentation.request.CreateBannerRequest
import kr.solve.domain.banner.presentation.request.UpdateBannerRequest
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

@Tag(name = "Admin - Banner", description = "배너 관리 API")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
@RestController
@RequestMapping("/admin/banners")
class AdminBannerController(
    private val adminBannerService: AdminBannerService,
) {
    @Operation(summary = "배너 목록 조회")
    @GetMapping
    fun getBanners() = adminBannerService.getBanners()

    @Operation(summary = "배너 상세 조회")
    @GetMapping("/{bannerId}")
    suspend fun getBanner(
        @PathVariable bannerId: UUID,
    ) = adminBannerService.getBanner(bannerId)

    @Operation(summary = "배너 생성")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    suspend fun createBanner(
        @Valid @RequestBody request: CreateBannerRequest,
    ) = adminBannerService.createBanner(request)

    @Operation(summary = "배너 수정")
    @PatchMapping("/{bannerId}")
    suspend fun updateBanner(
        @PathVariable bannerId: UUID,
        @Valid @RequestBody request: UpdateBannerRequest,
    ) = adminBannerService.updateBanner(bannerId, request)

    @Operation(summary = "배너 삭제")
    @DeleteMapping("/{bannerId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    suspend fun deleteBanner(
        @PathVariable bannerId: UUID,
    ) = adminBannerService.deleteBanner(bannerId)
}
