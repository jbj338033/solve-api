package kr.solve.domain.problem.domain.repository

import io.r2dbc.spi.Row
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactive.asFlow
import kr.solve.domain.problem.domain.entity.Problem
import kr.solve.domain.problem.domain.enums.ProblemDifficulty
import kr.solve.domain.problem.domain.enums.ProblemSort
import kr.solve.domain.problem.domain.enums.ProblemType
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.UUID

@Repository
class ProblemQueryRepository(
    private val databaseClient: DatabaseClient,
) {
    fun findWithFilters(
        cursor: UUID?,
        limit: Int,
        difficulties: List<ProblemDifficulty>?,
        type: ProblemType?,
        query: String?,
        tagIds: List<UUID>?,
        sort: ProblemSort,
    ): Flow<Problem> {
        val conditions = mutableListOf("p.is_public = true")
        val params = mutableMapOf<String, Any>()

        if (cursor != null) {
            conditions.add("p.id < :cursor")
            params["cursor"] = cursor
        }

        if (!difficulties.isNullOrEmpty()) {
            conditions.add("p.difficulty = ANY(:difficulties)")
            params["difficulties"] = difficulties.map { it.name }.toTypedArray()
        }

        if (type != null) {
            conditions.add("p.type = :type")
            params["type"] = type.name
        }

        if (!query.isNullOrBlank()) {
            conditions.add("p.title ILIKE :query")
            params["query"] = "%$query%"
        }

        if (!tagIds.isNullOrEmpty()) {
            conditions.add(
                """
                p.id IN (
                    SELECT pt.problem_id FROM problem_tags pt
                    WHERE pt.tag_id = ANY(:tagIds)
                    GROUP BY pt.problem_id
                    HAVING COUNT(DISTINCT pt.tag_id) = :tagCount
                )
                """.trimIndent(),
            )
            params["tagIds"] = tagIds.toTypedArray()
            params["tagCount"] = tagIds.size
        }

        val difficultyArray = ProblemDifficulty.entries.joinToString(", ") { "'${it.name}'" }
        val difficultyOrder = "array_position(ARRAY[$difficultyArray], p.difficulty)"

        val orderBy =
            when (sort) {
                ProblemSort.LATEST -> "p.id DESC"
                ProblemSort.DIFFICULTY_ASC -> "$difficultyOrder ASC, p.id DESC"
                ProblemSort.DIFFICULTY_DESC -> "$difficultyOrder DESC, p.id DESC"
                ProblemSort.ACCEPT_RATE_DESC ->
                    "CASE WHEN COALESCE(ps.submission_count, 0) = 0 THEN 0 ELSE COALESCE(ps.accepted_count, 0)::float / ps.submission_count END DESC, p.id DESC"
                ProblemSort.SUBMISSIONS_DESC -> "COALESCE(ps.submission_count, 0) DESC, p.id DESC"
                ProblemSort.ACCEPTED_USERS_DESC -> "COALESCE(ps.accepted_user_count, 0) DESC, p.id DESC"
            }

        val sql =
            """
            SELECT p.* FROM problems p
            LEFT JOIN problem_stats ps ON p.id = ps.problem_id
            WHERE ${conditions.joinToString(" AND ")}
            ORDER BY $orderBy
            LIMIT :limit
            """.trimIndent()

        params["limit"] = limit

        var spec = databaseClient.sql(sql)
        params.forEach { (key, value) -> spec = spec.bind(key, value) }

        return spec.map { row: Row, _ -> row.toProblem() }.all().asFlow()
    }

    private fun Row.toProblem() =
        Problem(
            id = get("id", UUID::class.java)!!,
            version = get("version", Long::class.javaObjectType)?.toLong(),
            createdAt = get("created_at", LocalDateTime::class.java),
            updatedAt = get("updated_at", LocalDateTime::class.java),
            number = get("number", Int::class.javaObjectType)!!,
            title = get("title", String::class.java)!!,
            description = get("description", String::class.java)!!,
            inputFormat = get("input_format", String::class.java)!!,
            outputFormat = get("output_format", String::class.java)!!,
            difficulty = ProblemDifficulty.valueOf(get("difficulty", String::class.java)!!),
            timeLimit = get("time_limit", Int::class.javaObjectType)!!,
            memoryLimit = get("memory_limit", Int::class.javaObjectType)!!,
            authorId = get("author_id", UUID::class.java)!!,
            isPublic = get("is_public", Boolean::class.javaObjectType)!!,
            type = ProblemType.valueOf(get("type", String::class.java)!!),
            checkerCode = get("checker_code", String::class.java),
            checkerLanguage = get("checker_language", String::class.java),
            interactorCode = get("interactor_code", String::class.java),
            interactorLanguage = get("interactor_language", String::class.java),
        )
}
