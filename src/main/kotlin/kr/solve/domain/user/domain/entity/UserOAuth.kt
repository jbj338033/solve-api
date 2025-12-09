package kr.solve.domain.user.domain.entity

import kr.solve.common.entity.BaseEntity
import kr.solve.domain.user.domain.enums.UserOAuthProvider
import org.springframework.data.relational.core.mapping.Table
import java.util.UUID

@Table("user_oauths")
data class UserOAuth(
    val userId: UUID,
    val provider: UserOAuthProvider,
    val providerId: String,
) : BaseEntity()
