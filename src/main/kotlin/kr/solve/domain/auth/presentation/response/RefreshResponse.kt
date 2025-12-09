package kr.solve.domain.auth.presentation.response

import io.swagger.v3.oas.annotations.media.Schema

@Schema(name = "Auth.Refresh", description = "토큰 갱신 응답")
data class RefreshResponse(
    @Schema(description = "새로운 액세스 토큰 (JWT)")
    val accessToken: String,
    @Schema(description = "새로운 리프레시 토큰")
    val refreshToken: String,
)
