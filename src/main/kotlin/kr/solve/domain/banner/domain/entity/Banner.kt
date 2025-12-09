package kr.solve.domain.banner.domain.entity

import kr.solve.common.entity.BaseEntity
import org.springframework.data.relational.core.mapping.Table

@Table("banners")
data class Banner(
    val name: String,
    val description: String,
    val imageUrl: String,
) : BaseEntity()
