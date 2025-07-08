package pt.isel.keepmyplanet.api

import io.ktor.http.HttpStatusCode
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receiveMultipart
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.utils.io.readRemaining
import kotlinx.io.readByteArray
import pt.isel.keepmyplanet.exception.ValidationException
import pt.isel.keepmyplanet.mapper.photo.toResponse
import pt.isel.keepmyplanet.service.PhotoService
import pt.isel.keepmyplanet.utils.getCurrentUserId
import pt.isel.keepmyplanet.utils.getPathUIntId

fun Route.photoWebApi(photoService: PhotoService) {
    route("/photos") {
        get("/{id}") {
            val photoId = call.getPathUIntId("id", "Photo ID")
            photoService
                .getPhotoDetails(photoId)
                .onSuccess { photo -> call.respond(HttpStatusCode.OK, photo.toResponse()) }
                .onFailure { throw it }
        }
        authenticate("auth-jwt") {
            post {
                val uploaderId = call.getCurrentUserId()
                var imageData: ByteArray? = null
                var filename: String? = null

                val multipart = call.receiveMultipart()
                multipart.forEachPart { part ->
                    if (part is PartData.FileItem && part.name == "image") {
                        filename = part.originalFileName
                        imageData = part.provider().readRemaining().readByteArray()
                    }
                    part.dispose()
                }

                if (imageData == null || filename == null) {
                    throw ValidationException(
                        "Multipart request must include an 'image' file part.",
                    )
                }

                photoService
                    .createPhoto(imageData, filename!!, uploaderId)
                    .onSuccess { photo -> call.respond(HttpStatusCode.Created, photo.toResponse()) }
                    .onFailure { throw it }
            }
        }
    }
}
