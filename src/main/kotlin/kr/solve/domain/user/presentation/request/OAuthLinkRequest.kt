package kr.solve.domain.user.presentation.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank

data class OAuthLinkRequest(
    @field:NotBlank
    @Schema(description = "Google: ID Token / GitHub: Authorization Code")
    val credential: String,
)
