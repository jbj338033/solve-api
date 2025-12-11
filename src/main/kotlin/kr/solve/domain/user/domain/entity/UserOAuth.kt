package kr.solve.domain.user.domain.entity

import com.github.f4b6a3.ulid.UlidCreator
import kr.solve.common.entity.BaseEntity
import kr.solve.domain.user.domain.enums.UserOAuthProvider
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Version
import org.springframework.data.relational.core.mapping.Table
import java.util.UUID

@Table("user_oauths")
data class UserOAuth(
    @Id val id: UUID = UlidCreator.getMonotonicUlid().toUuid(),
    @Version val version: Long? = null,
    val userId: UUID,
    val provider: UserOAuthProvider,
    val providerId: String,
) : BaseEntity()
