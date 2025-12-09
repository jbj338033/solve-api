package kr.solve.domain.auth.presentation.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank

data class OAuthLoginRequest(
    @field:NotBlank
    @Schema(description = "Google: ID Token / GitHub: Authorization Code")
    val credential: String,
)
