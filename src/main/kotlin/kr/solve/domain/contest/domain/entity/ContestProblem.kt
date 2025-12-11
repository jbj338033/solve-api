package kr.solve.domain.contest.domain.entity

import com.github.f4b6a3.ulid.UlidCreator
import kr.solve.common.entity.BaseEntity
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Version
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.util.UUID

@Table("contest_problems")
data class ContestProblem(
    @Id val id: UUID = UlidCreator.getMonotonicUlid().toUuid(),
    @Version val version: Long? = null,
    val contestId: UUID,
    val problemId: UUID,
    @Column("order") val order: Int,
    val score: Int? = null,
) : BaseEntity()
