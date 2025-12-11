package kr.solve.domain.user.domain.entity

import com.github.f4b6a3.ulid.UlidCreator
import kr.solve.domain.user.domain.enums.UserRole
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.annotation.Version
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

@Table("users")
data class User(
    @Id val id: UUID = UlidCreator.getMonotonicUlid().toUuid(),
    @Version val version: Long? = null,
    @CreatedDate @Column("created_at") val createdAt: LocalDateTime? = null,
    @LastModifiedDate @Column("updated_at") val updatedAt: LocalDateTime? = null,
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
)
