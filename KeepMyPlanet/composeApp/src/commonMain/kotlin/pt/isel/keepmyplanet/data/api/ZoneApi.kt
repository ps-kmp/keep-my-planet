package pt.isel.keepmyplanet.data.api

import io.ktor.client.HttpClient
import io.ktor.client.request.parameter
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.http.HttpMethod
import pt.isel.keepmyplanet.data.http.executeRequest
import pt.isel.keepmyplanet.data.http.executeRequestUnit
import pt.isel.keepmyplanet.dto.zone.AddPhotoRequest
import pt.isel.keepmyplanet.dto.zone.ConfirmCleanlinessRequest
import pt.isel.keepmyplanet.dto.zone.ReportZoneRequest
import pt.isel.keepmyplanet.dto.zone.UpdateZoneRequest
import pt.isel.keepmyplanet.dto.zone.ZoneResponse

class ZoneApi(
    private val httpClient: HttpClient,
) {
    private object Endpoints {
        const val ZONES_BASE = "zones"

        fun reportZone() = ZONES_BASE

        fun findAllZones() = ZONES_BASE

        fun findZonesByLocation() = ZONES_BASE

        fun zoneById(zoneId: UInt) = "$ZONES_BASE/$zoneId"

        fun updateZone(zoneId: UInt) = zoneById(zoneId)

        fun deleteZone(zoneId: UInt) = zoneById(zoneId)

        fun addPhotoToZone(zoneId: UInt) = "${zoneById(zoneId)}/photos"

        fun removePhotoFromZone(
            zoneId: UInt,
            photoId: UInt,
        ) = "${zoneById(zoneId)}/photos/$photoId"

        fun confirmCleanliness(zoneId: UInt) = "$ZONES_BASE/$zoneId/confirm-cleanliness"

        fun revertToReported(zoneId: UInt) = "$ZONES_BASE/$zoneId/revert-to-reported"
    }

    suspend fun reportZone(request: ReportZoneRequest): Result<ZoneResponse> =
        httpClient.executeRequest {
            method = HttpMethod.Post
            url(Endpoints.reportZone())
            setBody(request)
        }

    suspend fun getZoneDetails(zoneId: UInt): Result<ZoneResponse> =
        httpClient.executeRequest {
            method = HttpMethod.Get
            url(Endpoints.zoneById(zoneId))
        }

    suspend fun findAllZones(): Result<List<ZoneResponse>> =
        httpClient.executeRequest {
            method = HttpMethod.Get
            url(Endpoints.findAllZones())
        }

    suspend fun findZonesByLocation(
        latitude: Double,
        longitude: Double,
        radius: Double,
    ): Result<List<ZoneResponse>> =
        httpClient.executeRequest {
            method = HttpMethod.Get
            url(Endpoints.findZonesByLocation())
            parameter("lat", latitude)
            parameter("lon", longitude)
            parameter("radius", radius)
        }

    suspend fun updateZone(
        zoneId: UInt,
        request: UpdateZoneRequest,
    ): Result<ZoneResponse> =
        httpClient.executeRequest {
            method = HttpMethod.Patch
            url(Endpoints.updateZone(zoneId))
            setBody(request)
        }

    suspend fun deleteZone(zoneId: UInt): Result<Unit> =
        httpClient.executeRequestUnit {
            method = HttpMethod.Delete
            url(Endpoints.deleteZone(zoneId))
        }

    suspend fun addPhotoToZone(
        zoneId: UInt,
        photoId: UInt,
    ): Result<ZoneResponse> =
        httpClient.executeRequest {
            method = HttpMethod.Post
            url(Endpoints.addPhotoToZone(zoneId))
            setBody(AddPhotoRequest(photoId = photoId))
        }

    suspend fun removePhotoFromZone(
        zoneId: UInt,
        photoId: UInt,
    ): Result<ZoneResponse> =
        httpClient.executeRequest {
            method = HttpMethod.Delete
            url(Endpoints.removePhotoFromZone(zoneId, photoId))
        }

    suspend fun confirmCleanliness(
        zoneId: UInt,
        request: ConfirmCleanlinessRequest,
    ): Result<ZoneResponse> =
        httpClient.executeRequest {
            method = HttpMethod.Post
            url(Endpoints.confirmCleanliness(zoneId))
            setBody(request)
        }

    suspend fun revertToReported(zoneId: UInt): Result<ZoneResponse> =
        httpClient.executeRequest {
            method = HttpMethod.Post
            url("${Endpoints.zoneById(zoneId)}/revert-to-reported")
        }
}
