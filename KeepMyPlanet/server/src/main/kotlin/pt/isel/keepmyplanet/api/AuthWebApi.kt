package pt.isel.keepmyplanet.api

import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import pt.isel.keepmyplanet.dto.auth.LoginRequest
import pt.isel.keepmyplanet.service.AuthService

fun Route.authWebApi(authService: AuthService) {
    route("/auth") {
        post("/login") {
            val request = call.receive<LoginRequest>()
            authService
                .login(request)
                .onSuccess { response -> call.respond(HttpStatusCode.OK, response) }
                .onFailure { throw it }
        }
    }
}
