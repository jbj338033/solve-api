package kr.solve.domain.workbook.presentation.response

import io.swagger.v3.oas.annotations.media.Schema
import kr.solve.domain.problem.domain.enums.ProblemDifficulty
import kr.solve.domain.problem.domain.enums.ProblemType
import kr.solve.domain.user.domain.entity.User
import kr.solve.domain.workbook.domain.entity.Workbook
import java.time.LocalDateTime

fun Workbook.toSummary(author: User) =
    WorkbookResponse.Summary(
        id = id!!,
        title = title,
        description = description,
        author = WorkbookResponse.Author(author.id!!, author.username, author.displayName, author.profileImage),
        createdAt = createdAt,
    )

fun Workbook.toDetail(
    author: User,
    problems: List<WorkbookResponse.Problem>,
) = WorkbookResponse.Detail(
    id = id!!,
    title = title,
    description = description,
    author = WorkbookResponse.Author(author.id!!, author.username, author.displayName, author.profileImage),
    createdAt = createdAt,
    updatedAt = updatedAt,
    problems = problems,
)

object WorkbookResponse {
    @Schema(name = "Workbook.Author", description = "워크북 작성자 정보")
    data class Author(
        @Schema(description = "작성자 ID")
        val id: Long,
        @Schema(description = "작성자 사용자명", example = "johndoe")
        val username: String,
        @Schema(description = "작성자 표시 이름", example = "John Doe")
        val displayName: String,
        @Schema(description = "작성자 프로필 이미지 URL")
        val profileImage: String,
    )

    @Schema(name = "Workbook.Problem", description = "워크북 문제 정보")
    data class Problem(
        @Schema(description = "문제 ID")
        val id: Long,
        @Schema(description = "문제 제목", example = "A+B")
        val title: String,
        @Schema(description = "문제 난이도")
        val difficulty: ProblemDifficulty,
        @Schema(description = "문제 유형")
        val type: ProblemType,
    )

    @Schema(name = "Workbook.Summary", description = "워크북 요약 정보")
    data class Summary(
        @Schema(description = "워크북 ID")
        val id: Long,
        @Schema(description = "워크북 제목", example = "DP 입문")
        val title: String,
        @Schema(description = "워크북 설명")
        val description: String?,
        @Schema(description = "작성자 정보")
        val author: Author,
        @Schema(description = "생성일시")
        val createdAt: LocalDateTime?,
    )

    @Schema(name = "Workbook.Detail", description = "워크북 상세 정보")
    data class Detail(
        @Schema(description = "워크북 ID")
        val id: Long,
        @Schema(description = "워크북 제목", example = "DP 입문")
        val title: String,
        @Schema(description = "워크북 설명")
        val description: String?,
        @Schema(description = "작성자 정보")
        val author: Author,
        @Schema(description = "생성일시")
        val createdAt: LocalDateTime?,
        @Schema(description = "수정일시")
        val updatedAt: LocalDateTime?,
        @Schema(description = "문제 목록")
        val problems: List<Problem>,
    )
}
