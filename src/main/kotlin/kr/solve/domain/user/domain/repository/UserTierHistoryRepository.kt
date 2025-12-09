package kr.solve.domain.user.domain.repository

import kotlinx.coroutines.flow.Flow
import kr.solve.domain.user.domain.entity.UserTierHistory
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import java.util.UUID

interface UserTierHistoryRepository : CoroutineCrudRepository<UserTierHistory, UUID> {
    fun findAllByUserIdOrderByAchievedAtDesc(userId: UUID): Flow<UserTierHistory>
}
