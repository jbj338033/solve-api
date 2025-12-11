package kr.solve.global.security.filter

import kr.solve.global.security.jwt.JwtProvider
import org.springframework.http.HttpHeaders
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono

@Component
class JwtAuthenticationFilter(
    private val jwtProvider: JwtProvider,
) : WebFilter {
    companion object {
        private const val BEARER_PREFIX = "Bearer "
    }

    override fun filter(
        exchange: ServerWebExchange,
        chain: WebFilterChain,
    ): Mono<Void> {
        val token = extractToken(exchange)

        if (token != null && jwtProvider.validateToken(token)) {
            val tokenType = jwtProvider.getType(token)
            if (tokenType == JwtProvider.JwtType.ACCESS) {
                val userId = jwtProvider.getUserId(token)
                val role = jwtProvider.getRole(token)
                val authentication =
                    UsernamePasswordAuthenticationToken(
                        userId,
                        null,
                        listOf(SimpleGrantedAuthority("ROLE_$role")),
                    )
                return chain
                    .filter(exchange)
                    .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication))
            }
        }

        return chain.filter(exchange)
    }

    private fun extractToken(exchange: ServerWebExchange): String? {
        val authHeader = exchange.request.headers.getFirst(HttpHeaders.AUTHORIZATION)
        return if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
            authHeader.substring(BEARER_PREFIX.length)
        } else {
            null
        }
    }
}
