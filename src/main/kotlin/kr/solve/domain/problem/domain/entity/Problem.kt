package kr.solve.domain.problem.domain.entity

import com.github.f4b6a3.ulid.UlidCreator
import kr.solve.domain.problem.domain.enums.ProblemDifficulty
import kr.solve.domain.problem.domain.enums.ProblemType
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.annotation.Version
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime
import java.util.UUID

@Table("problems")
data class Problem(
    @Id val id: UUID = UlidCreator.getMonotonicUlid().toUuid(),
    @Version val version: Long? = null,
    @CreatedDate @Column("created_at") val createdAt: LocalDateTime? = null,
    @LastModifiedDate @Column("updated_at") val updatedAt: LocalDateTime? = null,
    val title: String,
    val description: String,
    val inputFormat: String,
    val outputFormat: String,
    val difficulty: ProblemDifficulty,
    val timeLimit: Int = 1000,
    val memoryLimit: Int = 256,
    val authorId: UUID,
    val isPublic: Boolean = false,
    val type: ProblemType = ProblemType.STANDARD,
    val checkerCode: String? = null,
    val checkerLanguage: String? = null,
    val interactorCode: String? = null,
    val interactorLanguage: String? = null,
)
