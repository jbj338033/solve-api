package kr.solve.domain.banner.presentation.request

import jakarta.validation.constraints.Size

data class AdminUpdateBannerRequest(
    @field:Size(max = 100)
    val name: String? = null,
    @field:Size(max = 500)
    val description: String? = null,
    val imageUrl: String? = null,
)
