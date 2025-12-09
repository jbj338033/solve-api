package kr.solve.common.entity

import com.github.f4b6a3.ulid.UlidCreator
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.annotation.Version
import org.springframework.data.relational.core.mapping.Column
import java.time.LocalDateTime
import java.util.UUID

abstract class BaseEntity {
    @Id
    var id: UUID = UlidCreator.getMonotonicUlid().toUuid()

    @Version
    var version: Long? = null

    @CreatedDate
    @Column("created_at")
    var createdAt: LocalDateTime? = null

    @LastModifiedDate
    @Column("updated_at")
    var updatedAt: LocalDateTime? = null
}
