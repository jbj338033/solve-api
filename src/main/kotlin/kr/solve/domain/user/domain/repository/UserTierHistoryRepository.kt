package kr.solve.domain.user.domain.repository

import kotlinx.coroutines.flow.Flow
import kr.solve.domain.user.domain.entity.UserTierHistory
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface UserTierHistoryRepository : CoroutineCrudRepository<UserTierHistory, Long> {
    fun findAllByUserIdOrderByAchievedAtDesc(userId: Long): Flow<UserTierHistory>
}
