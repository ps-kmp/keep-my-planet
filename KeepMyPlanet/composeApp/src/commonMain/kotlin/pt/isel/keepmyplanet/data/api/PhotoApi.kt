package pt.isel.keepmyplanet.data.api

import io.ktor.client.HttpClient
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import pt.isel.keepmyplanet.data.http.executeRequest
import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.dto.photo.PhotoResponse

class PhotoApi(
    private val httpClient: HttpClient,
) {
    private object Endpoints {
        const val PHOTOS_BASE = "photos"

        fun photoById(photoId: Id) = "$PHOTOS_BASE/${photoId.value}"
    }

    suspend fun createPhoto(
        imageData: ByteArray,
        filename: String,
    ): Result<PhotoResponse> =
        httpClient.executeRequest {
            method = HttpMethod.Post
            url(Endpoints.PHOTOS_BASE)
            setBody(
                MultiPartFormDataContent(
                    formData {
                        append(
                            "image",
                            imageData,
                            Headers.build {
                                append(HttpHeaders.ContentType, "image/jpeg")
                                append(HttpHeaders.ContentDisposition, "filename=\"$filename\"")
                            },
                        )
                    },
                ),
            )
        }

    suspend fun getPhotoById(photoId: Id): Result<PhotoResponse> =
        httpClient.executeRequest {
            method = HttpMethod.Get
            url(Endpoints.photoById(photoId))
        }
}
