package kr.solve.domain.banner.domain.repository

import kotlinx.coroutines.flow.Flow
import kr.solve.domain.banner.domain.entity.Banner
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface BannerRepository : CoroutineCrudRepository<Banner, Long> {
    fun findAllByOrderByNameAsc(): Flow<Banner>
}
