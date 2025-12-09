package kr.solve.domain.problem.domain.repository

import kotlinx.coroutines.flow.Flow
import kr.solve.domain.problem.domain.entity.ProblemTag
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import java.util.UUID

interface ProblemTagRepository : CoroutineCrudRepository<ProblemTag, UUID> {
    fun findAllByProblemId(problemId: UUID): Flow<ProblemTag>

    fun findAllByProblemIdIn(problemIds: List<UUID>): Flow<ProblemTag>

    fun findAllByTagId(tagId: UUID): Flow<ProblemTag>

    suspend fun deleteAllByProblemId(problemId: UUID)

    @Query("INSERT INTO problem_tags (problem_id, tag_id) VALUES (:problemId, :tagId)")
    suspend fun insert(
        problemId: UUID,
        tagId: UUID,
    )
}
