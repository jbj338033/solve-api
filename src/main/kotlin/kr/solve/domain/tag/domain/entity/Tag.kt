package kr.solve.domain.tag.domain.entity

import kr.solve.common.entity.BaseEntity
import org.springframework.data.relational.core.mapping.Table

@Table("tags")
data class Tag(
    val name: String,
) : BaseEntity()
