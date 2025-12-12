package kr.solve.global.security.jwt

import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import kr.solve.domain.auth.domain.error.AuthError
import kr.solve.domain.user.domain.enums.UserRole
import kr.solve.global.error.BusinessException
import org.springframework.stereotype.Component
import java.util.Date

@Component
class JwtProvider(
    private val jwtProperties: JwtProperties,
) {
    private val secretKey by lazy {
        Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtProperties.secret))
    }

    private val parser by lazy {
        Jwts.parser().verifyWith(secretKey).build()
    }

    fun createAccessToken(
        userId: Long,
        role: UserRole,
    ): String = createToken(userId, role, jwtProperties.accessTokenExpiration, JwtType.ACCESS)

    fun createRefreshToken(userId: Long): String = createToken(userId, null, jwtProperties.refreshTokenExpiration, JwtType.REFRESH)

    fun validateToken(token: String): Boolean = runCatching { parser.parseSignedClaims(token) }.isSuccess

    fun getUserId(token: String): Long = parse(token).subject.toLong()

    fun getType(token: String): JwtType = JwtType.valueOf(parse(token)[CLAIM_TYPE] as String)

    fun getRole(token: String): UserRole = UserRole.valueOf(parse(token)[CLAIM_ROLE] as String)

    private fun createToken(
        userId: Long,
        role: UserRole?,
        expiration: Long,
        type: JwtType,
    ): String =
        Jwts
            .builder()
            .subject(userId.toString())
            .claim(CLAIM_TYPE, type.name)
            .apply { role?.let { claim(CLAIM_ROLE, it.name) } }
            .issuedAt(Date())
            .expiration(Date(System.currentTimeMillis() + expiration))
            .signWith(secretKey)
            .compact()

    private fun parse(token: String) =
        try {
            parser.parseSignedClaims(token).payload
        } catch (e: ExpiredJwtException) {
            throw BusinessException(AuthError.EXPIRED_TOKEN)
        } catch (e: JwtException) {
            throw BusinessException(AuthError.INVALID_TOKEN)
        }

    enum class JwtType { ACCESS, REFRESH }

    companion object {
        private const val CLAIM_TYPE = "type"
        private const val CLAIM_ROLE = "role"
    }
}
