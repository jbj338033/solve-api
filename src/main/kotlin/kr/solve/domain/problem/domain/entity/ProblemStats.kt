package kr.solve.domain.problem.domain.entity

import com.github.f4b6a3.ulid.UlidCreator
import kr.solve.common.entity.BaseEntity
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Version
import org.springframework.data.relational.core.mapping.Table
import java.util.UUID

@Table("problem_stats")
data class ProblemStats(
    @Id val id: UUID = UlidCreator.getMonotonicUlid().toUuid(),
    @Version val version: Long? = null,
    val problemId: UUID,
    val submissionCount: Int = 0,
    val acceptedCount: Int = 0,
    val userCount: Int = 0,
    val acceptedUserCount: Int = 0,
) : BaseEntity()
