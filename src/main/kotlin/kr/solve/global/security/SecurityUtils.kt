package kr.solve.global.security

import kotlinx.coroutines.reactor.awaitSingleOrNull
import kr.solve.domain.auth.domain.error.AuthError
import kr.solve.global.error.BusinessException
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import java.util.UUID

suspend fun userId(): UUID =
    ReactiveSecurityContextHolder
        .getContext()
        .mapNotNull { it.authentication?.principal as? UUID }
        .awaitSingleOrNull()
        ?: throw BusinessException(AuthError.INVALID_TOKEN)
