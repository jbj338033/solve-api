package kr.solve.domain.banner.domain.repository

import kotlinx.coroutines.flow.Flow
import kr.solve.domain.banner.domain.entity.Banner
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import java.util.UUID

interface BannerRepository : CoroutineCrudRepository<Banner, UUID> {
    fun findAllByOrderByNameAsc(): Flow<Banner>
}
