package kr.solve.domain.contest.domain.entity

import com.github.f4b6a3.ulid.UlidCreator
import kr.solve.common.entity.BaseEntity
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Version
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime
import java.util.UUID

@Table("contest_results")
data class ContestResult(
    @Id val id: UUID = UlidCreator.getMonotonicUlid().toUuid(),
    @Version val version: Long? = null,
    val contestId: UUID,
    val userId: UUID,
    val problemId: UUID,
    val score: Int = 0,
    val attempts: Int = 0,
    val solvedAt: LocalDateTime? = null,
) : BaseEntity()
