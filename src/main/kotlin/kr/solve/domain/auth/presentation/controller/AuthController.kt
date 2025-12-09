package kr.solve.domain.auth.presentation.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import kr.solve.domain.auth.application.service.AuthService
import kr.solve.domain.auth.presentation.request.OAuthLoginRequest
import kr.solve.domain.auth.presentation.request.RefreshRequest
import kr.solve.domain.user.domain.enums.UserOAuthProvider
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Auth", description = "인증 API")
@RestController
@RequestMapping("/auth")
class AuthController(
    private val authService: AuthService,
) {
    @Operation(
        summary = "OAuth 로그인",
        description = """
OAuth 제공자를 통해 로그인하거나 신규 회원가입을 진행합니다.

| Provider | credential | 설명 |
|----------|------------|------|
| google | ID Token | Google Sign-In에서 반환되는 credential |
| github | Authorization Code | GitHub OAuth redirect로 전달받는 code |
        """,
    )
    @PostMapping("/{provider}")
    suspend fun login(
        @Parameter(description = "OAuth 제공자", schema = Schema(allowableValues = ["google", "github"]))
        @PathVariable provider: UserOAuthProvider,
        @Valid @RequestBody request: OAuthLoginRequest,
    ) = authService.login(provider, request)

    @Operation(summary = "토큰 갱신", description = "리프레시 토큰으로 새로운 토큰을 발급받습니다 (Refresh Token Rotation)")
    @PostMapping("/refresh")
    suspend fun refresh(
        @Valid @RequestBody request: RefreshRequest,
    ) = authService.refresh(request)

    @Operation(summary = "로그아웃", description = "리프레시 토큰을 무효화합니다")
    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    suspend fun logout(
        @Valid @RequestBody request: RefreshRequest,
    ) = authService.logout(request)
}
