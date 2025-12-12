package kr.solve.domain.submission.domain.repository

import io.r2dbc.spi.Row
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactive.asFlow
import kr.solve.domain.submission.domain.entity.Submission
import kr.solve.domain.submission.domain.enums.JudgeResult
import kr.solve.domain.submission.domain.enums.Language
import kr.solve.domain.submission.domain.enums.SubmissionStatus
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.UUID

@Repository
class SubmissionQueryRepository(
    private val databaseClient: DatabaseClient,
) {
    fun findWithFilters(
        cursor: UUID?,
        limit: Int,
        userId: UUID?,
        problemId: UUID?,
        language: Language?,
        result: JudgeResult?,
    ): Flow<Submission> {
        val conditions = mutableListOf<String>()
        val params = mutableMapOf<String, Any>()

        if (cursor != null) {
            conditions.add("id < :cursor")
            params["cursor"] = cursor
        }

        if (userId != null) {
            conditions.add("user_id = :userId")
            params["userId"] = userId
        }

        if (problemId != null) {
            conditions.add("problem_id = :problemId")
            params["problemId"] = problemId
        }

        if (language != null) {
            conditions.add("language = :language")
            params["language"] = language.name
        }

        if (result != null) {
            conditions.add("result = :result")
            params["result"] = result.name
        }

        val whereClause = if (conditions.isNotEmpty()) "WHERE ${conditions.joinToString(" AND ")}" else ""

        val sql = """
            SELECT * FROM submissions
            $whereClause
            ORDER BY id DESC
            LIMIT :limit
        """.trimIndent()

        params["limit"] = limit

        var spec = databaseClient.sql(sql)
        params.forEach { (key, value) -> spec = spec.bind(key, value) }

        return spec.map { row: Row, _ -> row.toSubmission() }.all().asFlow()
    }

    private fun Row.toSubmission() = Submission(
        id = get("id", UUID::class.java)!!,
        version = get("version", Long::class.javaObjectType)?.toLong(),
        createdAt = get("created_at", LocalDateTime::class.java),
        updatedAt = get("updated_at", LocalDateTime::class.java),
        problemId = get("problem_id", UUID::class.java)!!,
        userId = get("user_id", UUID::class.java)!!,
        contestId = get("contest_id", UUID::class.java),
        language = Language.valueOf(get("language", String::class.java)!!),
        code = get("code", String::class.java)!!,
        status = SubmissionStatus.valueOf(get("status", String::class.java)!!),
        result = get("result", String::class.java)?.let { JudgeResult.valueOf(it) },
        score = get("score", Int::class.javaObjectType),
        timeUsed = get("time_used", Int::class.javaObjectType),
        memoryUsed = get("memory_used", Int::class.javaObjectType),
        error = get("error", String::class.java),
        judgedAt = get("judged_at", LocalDateTime::class.java),
    )
}
