package pt.isel.keepmyplanet.api

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.patch
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.domain.user.Email
import pt.isel.keepmyplanet.domain.user.Name
import pt.isel.keepmyplanet.domain.user.Password
import pt.isel.keepmyplanet.dto.user.ChangePasswordRequest
import pt.isel.keepmyplanet.dto.user.RegisterRequest
import pt.isel.keepmyplanet.dto.user.UpdateProfileRequest
import pt.isel.keepmyplanet.mapper.user.toResponse
import pt.isel.keepmyplanet.service.UserService
import pt.isel.keepmyplanet.util.getCurrentUserId
import pt.isel.keepmyplanet.util.getPathUIntId

fun Route.userWebApi(userService: UserService) {
    route("/users") {
        post {
            val request = call.receive<RegisterRequest>()

            val name = Name(request.name)
            val email = Email(request.email)
            val password = Password(request.password)

            userService
                .registerUser(name, email, password)
                .onSuccess { user -> call.respond(HttpStatusCode.Created, user.toResponse()) }
                .onFailure { throw it }
        }

        get {
            userService
                .getAllUsers()
                .onSuccess { call.respond(HttpStatusCode.OK, it.map { user -> user.toResponse() }) }
                .onFailure { throw it }
        }

        route("/{id}") {
            fun ApplicationCall.getUserId(): Id = getPathUIntId("id", "User ID")

            get {
                val userId = call.getUserId()

                userService
                    .getUserDetails(userId)
                    .onSuccess { user -> call.respond(HttpStatusCode.OK, user.toResponse()) }
                    .onFailure { throw it }
            }

            authenticate("auth-jwt") {
                patch {
                    val userIdToUpdate = call.getUserId()
                    val actingUserId = call.getCurrentUserId()
                    val request = call.receive<UpdateProfileRequest>()

                    val name = request.name?.let { Name(it) }
                    val email = request.email?.let { Email(it) }
                    val profilePicId = request.profilePictureId?.let { Id(it) }

                    userService
                        .updateUserProfile(userIdToUpdate, actingUserId, name, email, profilePicId)
                        .onSuccess { user -> call.respond(HttpStatusCode.OK, user.toResponse()) }
                        .onFailure { throw it }
                }

                delete {
                    val userIdToDelete = call.getUserId()
                    val actingUserId = call.getCurrentUserId()

                    userService
                        .deleteUser(userIdToDelete, actingUserId)
                        .onSuccess { call.respond(HttpStatusCode.NoContent) }
                        .onFailure { throw it }
                }

                patch("/password") {
                    val userIdToUpdate = call.getUserId()
                    val actingUserId = call.getCurrentUserId()
                    val request = call.receive<ChangePasswordRequest>()

                    val oldPassword = Password(request.oldPassword)
                    val newPassword = Password(request.newPassword)

                    userService
                        .changePassword(userIdToUpdate, actingUserId, oldPassword, newPassword)
                        .onSuccess {
                            call.respond(HttpStatusCode.OK, mapOf("message" to "Password updated."))
                        }.onFailure { throw it }
                }
            }
        }
    }
}
