package kr.solve.common.entity

import org.springframework.data.relational.core.mapping.Column
import java.time.LocalDateTime

abstract class BaseEntity {
    @Column("created_at")
    var createdAt: LocalDateTime? = null

    @Column("updated_at")
    var updatedAt: LocalDateTime? = null
}
