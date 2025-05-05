package pt.isel.keepmyplanet.api

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.patch
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import pt.isel.keepmyplanet.domain.common.Description
import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.domain.common.Location
import pt.isel.keepmyplanet.domain.zone.Zone
import pt.isel.keepmyplanet.domain.zone.ZoneSeverity
import pt.isel.keepmyplanet.domain.zone.ZoneStatus
import pt.isel.keepmyplanet.dto.zone.AddPhotoRequest
import pt.isel.keepmyplanet.dto.zone.ReportZoneRequest
import pt.isel.keepmyplanet.dto.zone.UpdateSeverityRequest
import pt.isel.keepmyplanet.dto.zone.UpdateStatusRequest
import pt.isel.keepmyplanet.errors.ValidationException
import pt.isel.keepmyplanet.mapper.zone.toResponse
import pt.isel.keepmyplanet.service.ZoneService
import pt.isel.keepmyplanet.util.getPathUIntId
import pt.isel.keepmyplanet.util.getQueryDoubleParameter
import pt.isel.keepmyplanet.util.safeValueOf

fun Route.zoneWebApi(zoneService: ZoneService) {
    route("/zones") {
        post {
            val request = call.receive<ReportZoneRequest>()
            val reporterId = call.getCurrentUserId()
            val location = Location(request.latitude, request.longitude)
            val description = Description(request.description)
            val zoneSeverity = safeValueOf<ZoneSeverity>(request.severity) ?: ZoneSeverity.UNKNOWN
            val photoIds = request.photoIds.map { Id(it) }.toSet()

            zoneService
                .reportZone(location, description, photoIds, reporterId, zoneSeverity)
                .onSuccess { call.respond(HttpStatusCode.Created, it.toResponse()) }
                .onFailure { throw it }
        }

        get {
            val lat = call.getQueryDoubleParameter("lat")
            val lon = call.getQueryDoubleParameter("lon")
            val radius = call.getQueryDoubleParameter("radius")

            determineZoneResult(lat, lon, radius, zoneService)
                .onSuccess { z -> call.respond(HttpStatusCode.OK, z.map { it.toResponse() }) }
                .onFailure { throw it }
        }

        route("/{id}") {
            fun ApplicationCall.getZoneId(): Id = getPathUIntId("id", "Zone ID")

            get {
                val zoneId = call.getZoneId()
                zoneService
                    .getZoneDetails(zoneId)
                    .onSuccess { zone -> call.respond(HttpStatusCode.OK, zone.toResponse()) }
                    .onFailure { throw it }
            }

            delete {
                val zoneId = call.getZoneId()
                val userId = call.getCurrentUserId()
                zoneService
                    .deleteZone(zoneId, userId)
                    .onSuccess { call.respond(HttpStatusCode.NoContent) }
                    .onFailure { throw it }
            }

            patch("/status") {
                val zoneId = call.getZoneId()
                val userId = call.getCurrentUserId()
                val request = call.receive<UpdateStatusRequest>()
                val newStatus =
                    safeValueOf<ZoneStatus>(request.status)
                        ?: throw IllegalArgumentException("Invalid status value: ${request.status}")

                zoneService
                    .updateZoneStatus(zoneId, userId, newStatus)
                    .onSuccess { call.respond(HttpStatusCode.OK, it.toResponse()) }
                    .onFailure { throw it }
            }

            patch("/severity") {
                val zoneId = call.getZoneId()
                val userId = call.getCurrentUserId()
                val request = call.receive<UpdateSeverityRequest>()
                val newZoneSeverity =
                    safeValueOf<ZoneSeverity>(request.severity)
                        ?: throw IllegalArgumentException("Invalid severity value: ${request.severity}")

                zoneService
                    .updateZoneSeverity(zoneId, userId, newZoneSeverity)
                    .onSuccess { call.respond(HttpStatusCode.OK, it.toResponse()) }
                    .onFailure { throw it }
            }

            route("/photos") {
                post {
                    val zoneId = call.getZoneId()
                    val userId = call.getCurrentUserId()
                    val request = call.receive<AddPhotoRequest>()
                    val photoId = Id(request.photoId)

                    zoneService
                        .addPhotoToZone(zoneId, userId, photoId)
                        .onSuccess { call.respond(HttpStatusCode.OK, it.toResponse()) }
                        .onFailure { throw it }
                }

                delete("/{photoId}") {
                    fun ApplicationCall.getPhotoId(): Id = getPathUIntId("photoId", "Photo ID")
                    val zoneId = call.getZoneId()
                    val userId = call.getCurrentUserId()
                    val photoId = call.getPhotoId()

                    zoneService
                        .removePhotoFromZone(zoneId, userId, photoId)
                        .onSuccess { call.respond(HttpStatusCode.OK, it.toResponse()) }
                        .onFailure { throw it }
                }
            }
        }
    }
}

private suspend fun determineZoneResult(
    lat: Double?,
    lon: Double?,
    radius: Double?,
    zoneService: ZoneService,
): Result<List<Zone>> =
    if (lat != null && lon != null && radius != null) {
        if (radius <= 0) {
            Result.failure(ValidationException("Query parameter 'radius' must be positive."))
        } else {
            zoneService.findZones(Location(lat, lon), radius)
        }
    } else if (lat == null && lon == null && radius == null) {
        zoneService.findAll()
    } else {
        Result.failure(
            ValidationException(
                "Query parameters 'lat', 'lon', and 'radius' must all be provided together for location filtering.",
            ),
        )
    }
