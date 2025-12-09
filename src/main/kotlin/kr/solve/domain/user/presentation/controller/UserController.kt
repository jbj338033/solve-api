package kr.solve.domain.user.presentation.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import kr.solve.domain.user.application.service.UserService
import kr.solve.domain.user.domain.enums.RatingType
import kr.solve.domain.user.domain.enums.UserOAuthProvider
import kr.solve.domain.user.presentation.request.OAuthLinkRequest
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Tag(name = "User", description = "사용자 API")
@RestController
@RequestMapping("/users")
class UserController(
    private val userService: UserService,
) {
    @Operation(summary = "사용자 랭킹 조회")
    @GetMapping("/ranking")
    suspend fun getRanking(
        @Parameter(description = "레이팅 타입", schema = Schema(allowableValues = ["problem", "contest"]))
        @RequestParam(defaultValue = "problem") type: RatingType,
    ) = userService.getRanking(type)

    @Operation(summary = "내 정보 조회", security = [SecurityRequirement(name = "bearerAuth")])
    @GetMapping("/me")
    suspend fun getMyInfo() = userService.getMe()

    @Operation(summary = "사용자 프로필 조회")
    @GetMapping("/{username}")
    suspend fun getProfile(
        @PathVariable username: String,
    ) = userService.getByUsername(username)

    @Operation(
        summary = "OAuth 연동 추가",
        description = "현재 계정에 OAuth 제공자를 연동합니다",
        security = [SecurityRequirement(name = "bearerAuth")],
    )
    @PostMapping("/me/oauth/{provider}")
    suspend fun linkOAuth(
        @Parameter(description = "OAuth 제공자", schema = Schema(allowableValues = ["google", "github"]))
        @PathVariable provider: UserOAuthProvider,
        @Valid @RequestBody request: OAuthLinkRequest,
    ) = userService.linkOAuth(provider, request)

    @Operation(
        summary = "OAuth 연동 해제",
        description = "마지막 OAuth 연동은 해제할 수 없습니다",
        security = [SecurityRequirement(name = "bearerAuth")],
    )
    @DeleteMapping("/me/oauth/{provider}")
    suspend fun unlinkOAuth(
        @Parameter(description = "OAuth 제공자", schema = Schema(allowableValues = ["google", "github"]))
        @PathVariable provider: UserOAuthProvider,
    ) = userService.unlinkOAuth(provider)

    @Operation(summary = "사용자 활동 기록 조회 (잔디)")
    @GetMapping("/{username}/activities")
    suspend fun getActivities(
        @PathVariable username: String,
        @Parameter(description = "연도 (미지정 시 최근 1년)")
        @RequestParam(required = false) year: Int?,
    ) = userService.getActivities(username, year)

    @Operation(summary = "사용자 레이팅 히스토리 조회")
    @GetMapping("/{username}/rating-history")
    suspend fun getRatingHistory(
        @PathVariable username: String,
        @Parameter(schema = Schema(allowableValues = ["PROBLEM", "CONTEST"]))
        @RequestParam(defaultValue = "PROBLEM") type: RatingType,
    ) = userService.getRatingHistory(username, type)

    @Operation(summary = "사용자 통계 조회")
    @GetMapping("/{username}/stats")
    suspend fun getStats(
        @PathVariable username: String,
    ) = userService.getStats(username)
}
