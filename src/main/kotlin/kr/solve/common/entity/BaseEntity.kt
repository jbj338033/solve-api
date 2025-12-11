package kr.solve.common.entity

import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.relational.core.mapping.Column
import java.time.LocalDateTime

abstract class BaseEntity {
    @CreatedDate
    @Column("created_at")
    var createdAt: LocalDateTime? = null

    @LastModifiedDate
    @Column("updated_at")
    var updatedAt: LocalDateTime? = null
}
