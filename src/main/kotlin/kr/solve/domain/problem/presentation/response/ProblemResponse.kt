package kr.solve.domain.problem.presentation.response

import io.swagger.v3.oas.annotations.media.Schema
import kr.solve.domain.problem.domain.entity.Problem
import kr.solve.domain.problem.domain.enums.ProblemDifficulty
import kr.solve.domain.problem.domain.enums.ProblemType
import kr.solve.domain.problem.domain.enums.SolveStatus
import kr.solve.domain.user.domain.entity.User
import java.time.LocalDateTime
import java.util.UUID

fun Problem.toSummary(
    author: User,
    status: SolveStatus? = null,
) = ProblemResponse.Summary(
    id = id,
    title = title,
    difficulty = difficulty,
    author = ProblemResponse.Author(author.id, author.username, author.displayName, author.profileImage),
    isPublic = isPublic,
    type = type,
    status = status,
    createdAt = createdAt,
)

fun Problem.toDetail(
    author: User,
    examples: List<ProblemResponse.Example>,
    tags: List<ProblemResponse.Tag>,
    status: SolveStatus? = null,
) = ProblemResponse.Detail(
    id = id,
    title = title,
    difficulty = difficulty,
    author = ProblemResponse.Author(author.id, author.username, author.displayName, author.profileImage),
    isPublic = isPublic,
    type = type,
    status = status,
    createdAt = createdAt,
    description = description,
    inputFormat = inputFormat,
    outputFormat = outputFormat,
    timeLimit = timeLimit,
    memoryLimit = memoryLimit,
    checkerCode = checkerCode,
    checkerLanguage = checkerLanguage,
    interactorCode = interactorCode,
    interactorLanguage = interactorLanguage,
    examples = examples,
    tags = tags,
    updatedAt = updatedAt,
)

object ProblemResponse {
    @Schema(name = "Problem.Author", description = "문제 작성자 정보")
    data class Author(
        @Schema(description = "작성자 ID")
        val id: UUID,
        @Schema(description = "작성자 사용자명", example = "johndoe")
        val username: String,
        @Schema(description = "작성자 표시 이름", example = "John Doe")
        val displayName: String,
        @Schema(description = "작성자 프로필 이미지 URL")
        val profileImage: String,
    )

    @Schema(name = "Problem.Example", description = "문제 예제")
    data class Example(
        @Schema(description = "입력 예제", example = "1 2")
        val input: String,
        @Schema(description = "출력 예제", example = "3")
        val output: String,
        @Schema(description = "예제 순서", example = "0")
        val order: Int,
    )

    @Schema(name = "Problem.Tag", description = "문제 태그")
    data class Tag(
        @Schema(description = "태그 ID")
        val id: UUID,
        @Schema(description = "태그 이름", example = "DP")
        val name: String,
    )

    @Schema(name = "Problem.Summary", description = "문제 요약 정보")
    data class Summary(
        @Schema(description = "문제 ID")
        val id: UUID,
        @Schema(description = "문제 제목", example = "A+B")
        val title: String,
        @Schema(description = "문제 난이도")
        val difficulty: ProblemDifficulty,
        @Schema(description = "문제 작성자")
        val author: Author,
        @Schema(description = "공개 여부")
        val isPublic: Boolean,
        @Schema(description = "문제 유형")
        val type: ProblemType,
        @Schema(description = "풀이 상태 (로그인 시)")
        val status: SolveStatus?,
        @Schema(description = "생성일시")
        val createdAt: LocalDateTime?,
    )

    @Schema(name = "Problem.Detail", description = "문제 상세 정보")
    data class Detail(
        @Schema(description = "문제 ID")
        val id: UUID,
        @Schema(description = "문제 제목", example = "A+B")
        val title: String,
        @Schema(description = "문제 난이도")
        val difficulty: ProblemDifficulty,
        @Schema(description = "문제 작성자")
        val author: Author,
        @Schema(description = "공개 여부")
        val isPublic: Boolean,
        @Schema(description = "문제 유형")
        val type: ProblemType,
        @Schema(description = "풀이 상태 (로그인 시)")
        val status: SolveStatus?,
        @Schema(description = "생성일시")
        val createdAt: LocalDateTime?,
        @Schema(description = "문제 설명")
        val description: String,
        @Schema(description = "입력 형식 설명")
        val inputFormat: String,
        @Schema(description = "출력 형식 설명")
        val outputFormat: String,
        @Schema(description = "시간 제한 (ms)", example = "1000")
        val timeLimit: Int,
        @Schema(description = "메모리 제한 (MB)", example = "256")
        val memoryLimit: Int,
        @Schema(description = "스페셜 저지 코드")
        val checkerCode: String?,
        @Schema(description = "스페셜 저지 언어")
        val checkerLanguage: String?,
        @Schema(description = "인터랙터 코드")
        val interactorCode: String?,
        @Schema(description = "인터랙터 언어")
        val interactorLanguage: String?,
        @Schema(description = "예제 목록")
        val examples: List<Example>,
        @Schema(description = "태그 목록")
        val tags: List<Tag>,
        @Schema(description = "수정일시")
        val updatedAt: LocalDateTime?,
    )
}
