package kr.solve.domain.workbook.domain.entity

import kr.solve.common.entity.BaseEntity
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.util.UUID

@Table("workbook_problems")
data class WorkbookProblem(
    val workbookId: UUID,
    val problemId: UUID,
    @Column("order")
    val order: Int,
) : BaseEntity()
