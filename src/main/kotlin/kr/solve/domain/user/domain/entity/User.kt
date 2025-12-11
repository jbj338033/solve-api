package kr.solve.domain.user.domain.entity

import com.github.f4b6a3.ulid.UlidCreator
import kr.solve.common.entity.BaseEntity
import kr.solve.domain.user.domain.enums.UserRole
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Version
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDate
import java.util.UUID

@Table("users")
data class User(
    @Id val id: UUID = UlidCreator.getMonotonicUlid().toUuid(),
    @Version val version: Long? = null,
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
    val lastSolvedDate: LocalDate? = null,
    val selectedBannerId: UUID? = null,
    val role: UserRole,
) : BaseEntity()
