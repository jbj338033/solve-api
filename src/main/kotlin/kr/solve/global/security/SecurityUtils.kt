package kr.solve.global.security

import kotlinx.coroutines.reactor.awaitSingleOrNull
import kr.solve.domain.auth.domain.error.AuthError
import kr.solve.global.error.BusinessException
import org.springframework.security.core.context.ReactiveSecurityContextHolder

suspend fun userId(): Long = userIdOrNull() ?: throw BusinessException(AuthError.INVALID_TOKEN)

suspend fun userIdOrNull(): Long? =
    ReactiveSecurityContextHolder
        .getContext()
        .mapNotNull { it.authentication?.principal as? Long }
        .awaitSingleOrNull()
