package kr.solve.domain.banner.domain.entity

import com.github.f4b6a3.ulid.UlidCreator
import kr.solve.common.entity.BaseEntity
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Version
import org.springframework.data.relational.core.mapping.Table
import java.util.UUID

@Table("banners")
data class Banner(
    @Id val id: UUID = UlidCreator.getMonotonicUlid().toUuid(),
    @Version val version: Long? = null,
    val name: String,
    val description: String,
    val imageUrl: String,
) : BaseEntity()
