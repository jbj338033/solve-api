package kr.solve.domain.user.domain.entity

import com.github.f4b6a3.ulid.UlidCreator
import kr.solve.common.entity.BaseEntity
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Version
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDate
import java.util.UUID

@Table("user_activities")
data class UserActivity(
    @Id val id: UUID = UlidCreator.getMonotonicUlid().toUuid(),
    @Version val version: Long? = null,
    val userId: UUID,
    val date: LocalDate,
    val solvedCount: Int = 0,
    val submissionCount: Int = 0,
) : BaseEntity()
