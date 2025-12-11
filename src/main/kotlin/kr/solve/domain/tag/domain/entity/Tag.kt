package kr.solve.domain.tag.domain.entity

import com.github.f4b6a3.ulid.UlidCreator
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.annotation.Version
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime
import java.util.UUID

@Table("tags")
data class Tag(
    @Id val id: UUID = UlidCreator.getMonotonicUlid().toUuid(),
    @Version val version: Long? = null,
    @CreatedDate @Column("created_at") val createdAt: LocalDateTime? = null,
    @LastModifiedDate @Column("updated_at") val updatedAt: LocalDateTime? = null,
    val name: String,
)
