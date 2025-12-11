package kr.solve.domain.file.presentation.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive
import kr.solve.domain.file.domain.enums.FileType

@Schema(name = "CreatePresignedUrl", description = "Presigned URL 생성 요청")
data class CreatePresignedUrlRequest(
    @Schema(
        description = "파일 타입 (PROFILE: 5MB, jpeg/png/webp | BANNER: 10MB, jpeg/png/webp | PROBLEM_IMAGE: 10MB, jpeg/png/webp/gif)",
        example = "PROFILE",
    )
    val type: FileType,
    @Schema(description = "Content-Type", example = "image/png")
    @field:NotBlank
    val contentType: String,
    @Schema(description = "파일 크기 (bytes)", example = "1048576")
    @field:Positive
    val size: Long,
)
