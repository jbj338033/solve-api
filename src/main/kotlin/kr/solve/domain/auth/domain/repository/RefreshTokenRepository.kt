package kr.solve.domain.auth.domain.repository

import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import kr.solve.global.security.jwt.JwtProperties
import org.springframework.data.redis.core.ReactiveStringRedisTemplate
import org.springframework.stereotype.Repository
import java.time.Duration

@Repository
class RefreshTokenRepository(
    private val redisTemplate: ReactiveStringRedisTemplate,
    private val jwtProperties: JwtProperties,
) {
    suspend fun save(
        token: String,
        userId: Long,
    ) {
        val key = KEY_PREFIX + token
        val ttl = Duration.ofMillis(jwtProperties.refreshTokenExpiration)

        redisTemplate
            .opsForValue()
            .set(key, userId.toString(), ttl)
            .awaitSingle()
    }

    suspend fun findUserIdByToken(token: String): Long? {
        val key = KEY_PREFIX + token

        return redisTemplate
            .opsForValue()
            .get(key)
            .awaitSingleOrNull()
            ?.toLongOrNull()
    }

    suspend fun deleteByToken(token: String) {
        val key = KEY_PREFIX + token
        redisTemplate.delete(key).awaitSingleOrNull()
    }

    companion object {
        private const val KEY_PREFIX = "refresh_token:"
    }
}
