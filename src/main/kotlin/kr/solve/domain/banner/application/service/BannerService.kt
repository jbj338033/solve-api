package kr.solve.domain.banner.application.service

import kotlinx.coroutines.flow.toList
import kr.solve.domain.banner.domain.error.BannerError
import kr.solve.domain.banner.domain.repository.BannerRepository
import kr.solve.domain.banner.domain.repository.UserBannerRepository
import kr.solve.domain.banner.presentation.response.BannerResponse
import kr.solve.domain.banner.presentation.response.toAcquired
import kr.solve.domain.banner.presentation.response.toResponse
import kr.solve.domain.user.domain.error.UserError
import kr.solve.domain.user.domain.repository.UserRepository
import kr.solve.global.error.BusinessException
import kr.solve.global.security.userId
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class BannerService(
    private val bannerRepository: BannerRepository,
    private val userBannerRepository: UserBannerRepository,
    private val userRepository: UserRepository,
) {
    suspend fun getBanners(): List<BannerResponse.Summary> =
        bannerRepository
            .findAllByOrderByNameAsc()
            .toList()
            .map { it.toResponse() }

    suspend fun getMyBanners(): List<BannerResponse.Acquired> {
        val userId = userId()
        val userBanners = userBannerRepository.findAllByUserId(userId).toList()
        val bannerIds = userBanners.map { it.bannerId }
        val banners = bannerRepository.findAllById(bannerIds).toList().associateBy { it.id }

        return userBanners.mapNotNull { userBanner ->
            banners[userBanner.bannerId]?.toAcquired(userBanner)
        }
    }

    @Transactional
    suspend fun selectBanner(bannerId: UUID): BannerResponse.Summary {
        val userId = userId()

        val banner =
            bannerRepository.findById(bannerId)
                ?: throw BusinessException(BannerError.NOT_FOUND)

        if (!userBannerRepository.existsByUserIdAndBannerId(userId, bannerId)) {
            throw BusinessException(BannerError.NOT_ACQUIRED)
        }

        val user =
            userRepository.findById(userId)
                ?: throw BusinessException(UserError.NOT_FOUND)

        userRepository.save(user.copy(selectedBannerId = bannerId))

        return banner.toResponse()
    }
}
