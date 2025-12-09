package kr.solve.domain.workbook.domain.entity

import kr.solve.common.entity.BaseEntity
import org.springframework.data.relational.core.mapping.Table
import java.util.UUID

@Table("workbooks")
data class Workbook(
    val title: String,
    val description: String? = null,
    val authorId: UUID,
) : BaseEntity()
