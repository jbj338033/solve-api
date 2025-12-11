package kr.solve.domain.banner.application.service

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kr.solve.domain.banner.domain.entity.Banner
import kr.solve.domain.banner.domain.error.BannerError
import kr.solve.domain.banner.domain.repository.BannerRepository
import kr.solve.domain.banner.presentation.request.AdminCreateBannerRequest
import kr.solve.domain.banner.presentation.request.AdminUpdateBannerRequest
import kr.solve.domain.banner.presentation.response.AdminBannerResponse
import kr.solve.domain.banner.presentation.response.toAdminResponse
import kr.solve.global.error.BusinessException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class AdminBannerService(
    private val bannerRepository: BannerRepository,
) {
    fun getBanners(): Flow<AdminBannerResponse> = bannerRepository.findAllByOrderByNameAsc().map { it.toAdminResponse() }

    suspend fun getBanner(bannerId: UUID): AdminBannerResponse {
        val banner =
            bannerRepository.findById(bannerId)
                ?: throw BusinessException(BannerError.NOT_FOUND)
        return banner.toAdminResponse()
    }

    @Transactional
    suspend fun createBanner(request: AdminCreateBannerRequest) {
        bannerRepository.save(
            Banner(
                name = request.name,
                description = request.description,
                imageUrl = request.imageUrl,
            ),
        )
    }

    @Transactional
    suspend fun updateBanner(
        bannerId: UUID,
        request: AdminUpdateBannerRequest,
    ) {
        val banner =
            bannerRepository.findById(bannerId)
                ?: throw BusinessException(BannerError.NOT_FOUND)

        bannerRepository.save(
            banner.copy(
                name = request.name ?: banner.name,
                description = request.description ?: banner.description,
                imageUrl = request.imageUrl ?: banner.imageUrl,
            ),
        )
    }

    @Transactional
    suspend fun deleteBanner(bannerId: UUID) {
        val banner =
            bannerRepository.findById(bannerId)
                ?: throw BusinessException(BannerError.NOT_FOUND)
        bannerRepository.delete(banner)
    }
}
