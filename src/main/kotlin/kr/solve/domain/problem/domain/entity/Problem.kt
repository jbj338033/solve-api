package kr.solve.domain.problem.domain.entity

import com.github.f4b6a3.ulid.UlidCreator
import kr.solve.common.entity.BaseEntity
import kr.solve.domain.problem.domain.enums.ProblemType
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Version
import org.springframework.data.relational.core.mapping.Table
import java.util.UUID

@Table("problems")
data class Problem(
    @Id val id: UUID = UlidCreator.getMonotonicUlid().toUuid(),
    @Version val version: Long? = null,
    val title: String,
    val description: String,
    val inputFormat: String,
    val outputFormat: String,
    val difficulty: Int,
    val timeLimit: Int = 1000,
    val memoryLimit: Int = 256,
    val authorId: UUID,
    val isPublic: Boolean = false,
    val type: ProblemType = ProblemType.STANDARD,
    val checkerCode: String? = null,
    val checkerLanguage: String? = null,
    val interactorCode: String? = null,
    val interactorLanguage: String? = null,
) : BaseEntity()
