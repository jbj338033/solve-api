package kr.solve.domain.problem.domain.repository

import kotlinx.coroutines.flow.Flow
import kr.solve.domain.problem.domain.entity.ProblemTag
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface ProblemTagRepository : CoroutineCrudRepository<ProblemTag, Long> {
    fun findAllByProblemId(problemId: Long): Flow<ProblemTag>

    fun findAllByProblemIdIn(problemIds: List<Long>): Flow<ProblemTag>

    fun findAllByTagId(tagId: Long): Flow<ProblemTag>

    suspend fun deleteAllByProblemId(problemId: Long)

    @Query("INSERT INTO problem_tags (problem_id, tag_id) VALUES (:problemId, :tagId)")
    suspend fun insert(
        problemId: Long,
        tagId: Long,
    )
}
