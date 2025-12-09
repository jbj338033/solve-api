package kr.solve.domain.user.domain.entity

import kr.solve.common.entity.BaseEntity
import kr.solve.domain.user.domain.enums.UserRole
import org.springframework.data.relational.core.mapping.Table
import java.util.UUID

@Table("users")
data class User(
    val username: String,
    val displayName: String,
    val email: String,
    val profileImage: String,
    val bio: String,
    val organization: String,
    val problemRating: Int,
    val contestRating: Int,
    val currentStreak: Int = 0,
    val maxStreak: Int = 0,
    val lastSolvedDate: java.time.LocalDate? = null,
    val selectedBannerId: UUID? = null,
    val role: UserRole,
) : BaseEntity()
