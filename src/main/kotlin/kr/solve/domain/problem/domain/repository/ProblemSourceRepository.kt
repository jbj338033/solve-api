package kr.solve.domain.problem.domain.repository

import kr.solve.domain.problem.domain.entity.ProblemSource
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface ProblemSourceRepository : CoroutineCrudRepository<ProblemSource, Long> {
    suspend fun findByProblemId(problemId: Long): ProblemSource?
    suspend fun deleteByProblemId(problemId: Long)

    @Query("""
        INSERT INTO problem_sources (problem_id, solution_code, solution_language, generator_code, generator_language)
        VALUES (:problemId, :solutionCode, :solutionLanguage, :generatorCode, :generatorLanguage)
        ON CONFLICT (problem_id) DO UPDATE SET
            solution_code = :solutionCode,
            solution_language = :solutionLanguage,
            generator_code = :generatorCode,
            generator_language = :generatorLanguage
    """)
    suspend fun upsert(
        problemId: Long,
        solutionCode: String,
        solutionLanguage: String,
        generatorCode: String?,
        generatorLanguage: String?,
    )
}
