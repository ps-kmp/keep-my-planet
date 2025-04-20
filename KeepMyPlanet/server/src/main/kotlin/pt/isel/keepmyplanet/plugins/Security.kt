package pt.isel.keepmyplanet.plugins

import com.auth0.jwk.JwkProviderBuilder
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.response.respond
import java.util.concurrent.TimeUnit

private const val JWT_AUTH_PROVIDER_NAME = "auth-jwt"

fun Application.configureAuthentication() {
    val jwtIssuer = environment.config.property("jwt.issuer").getString()
    val jwtRealm = environment.config.property("jwt.realm").getString()
    val jwkProvider =
        JwkProviderBuilder(jwtIssuer)
            .cached(10, 24, TimeUnit.HOURS)
            .rateLimited(10, 1, TimeUnit.MINUTES)
            .build()

    install(Authentication) {
        jwt(JWT_AUTH_PROVIDER_NAME) {
            realm = jwtRealm

            verifier(jwkProvider, jwtIssuer) { acceptLeeway(3) }

            validate { credential ->
                if (credential.payload.getClaim("userId").asString() != "") {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }

            challenge { _, _ ->
                call.respond(
                    HttpStatusCode.Unauthorized,
                    "Token is not valid or has expired",
                )
            }
        }
    }
}
