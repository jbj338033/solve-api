package kr.solve.domain.banner.presentation.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class AdminCreateBannerRequest(
    @field:NotBlank
    @field:Size(max = 100)
    val name: String,
    @field:NotBlank
    @field:Size(max = 500)
    val description: String,
    @field:NotBlank
    val imageUrl: String,
)
