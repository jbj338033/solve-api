package kr.solve.domain.banner.domain.entity

import com.github.f4b6a3.ulid.UlidCreator
import kr.solve.common.entity.BaseEntity
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Version
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime
import java.util.UUID

@Table("user_banners")
data class UserBanner(
    @Id val id: UUID = UlidCreator.getMonotonicUlid().toUuid(),
    @Version val version: Long? = null,
    val userId: UUID,
    val bannerId: UUID,
    val acquiredAt: LocalDateTime = LocalDateTime.now(),
) : BaseEntity()
