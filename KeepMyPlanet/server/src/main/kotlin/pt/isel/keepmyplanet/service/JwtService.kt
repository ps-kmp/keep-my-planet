package pt.isel.keepmyplanet.service

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.DecodedJWT
import io.ktor.server.config.ApplicationConfig
import pt.isel.keepmyplanet.domain.common.Id
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlin.time.ExperimentalTime
import kotlin.time.toJavaInstant

class JwtService(
    config: ApplicationConfig,
) {
    private val secret = config.property("jwt.secret").getString()
    private val issuer = config.property("jwt.issuer").getString()
    private val audience = config.property("jwt.audience").getString()
    private val validity = 1.hours
    private val algorithm = Algorithm.HMAC256(secret)

    companion object {
        private const val USER_ID_CLAIM = "userId"
    }

    @OptIn(ExperimentalTime::class)
    fun generateToken(userId: Id): String {
        val now = Clock.System.now()
        val expiresAt = now.plus(validity)

        return JWT
            .create()
            .withAudience(audience)
            .withIssuer(issuer)
            .withClaim(USER_ID_CLAIM, userId.value.toString())
            .withIssuedAt(now.toJavaInstant())
            .withExpiresAt(expiresAt.toJavaInstant())
            .sign(algorithm)
    }

    fun verifyToken(token: String): DecodedJWT? =
        try {
            JWT
                .require(algorithm)
                .withIssuer(issuer)
                .withAudience(audience)
                .build()
                .verify(token)
        } catch (_: Exception) {
            null
        }
}
