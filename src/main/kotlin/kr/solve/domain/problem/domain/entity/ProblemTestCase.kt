package kr.solve.domain.problem.domain.entity

import com.github.f4b6a3.ulid.UlidCreator
import kr.solve.common.entity.BaseEntity
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Version
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.util.UUID

@Table("problem_test_cases")
data class ProblemTestCase(
    @Id val id: UUID = UlidCreator.getMonotonicUlid().toUuid(),
    @Version val version: Long? = null,
    val problemId: UUID,
    val input: String,
    val output: String,
    @Column("order") val order: Int,
    val subtaskId: UUID? = null,
) : BaseEntity()
