package kr.solve.global.security.jwt

import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import kr.solve.domain.auth.domain.error.AuthError
import kr.solve.global.error.BusinessException
import org.springframework.stereotype.Component
import java.util.Date
import java.util.UUID

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

    fun createAccessToken(userId: UUID): String = createToken(userId, jwtProperties.accessTokenExpiration, JwtType.ACCESS)

    fun createRefreshToken(userId: UUID): String = createToken(userId, jwtProperties.refreshTokenExpiration, JwtType.REFRESH)

    fun validateToken(token: String): Boolean = runCatching { parser.parseSignedClaims(token) }.isSuccess

    fun getUserId(token: String): UUID = UUID.fromString(parse(token).subject)

    fun getType(token: String): JwtType = JwtType.valueOf(parse(token)[CLAIM_TYPE] as String)

    private fun createToken(
        userId: UUID,
        expiration: Long,
        type: JwtType,
    ): String =
        Jwts
            .builder()
            .subject(userId.toString())
            .claim(CLAIM_TYPE, type.name)
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
    }
}
