package kr.solve.domain.file.presentation.response

import io.swagger.v3.oas.annotations.media.Schema

@Schema(name = "PresignedUrl", description = "Presigned URL 응답")
data class PresignedUrlResponse(
    @Schema(description = "업로드 URL")
    val uploadUrl: String,
    @Schema(description = "파일 접근 URL")
    val fileUrl: String,
    @Schema(description = "만료 시간 (초)")
    val expiresIn: Long,
)
