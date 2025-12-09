package kr.solve.domain.auth.presentation.response

import io.swagger.v3.oas.annotations.media.Schema
import kr.solve.domain.user.presentation.response.UserResponse

@Schema(name = "Auth.Login", description = "로그인 응답")
data class LoginResponse(
    @Schema(description = "액세스 토큰 (JWT)")
    val accessToken: String,
    @Schema(description = "리프레시 토큰")
    val refreshToken: String,
    @Schema(description = "사용자 정보")
    val user: UserResponse.Me,
)
