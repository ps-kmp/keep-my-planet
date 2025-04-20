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
import pt.isel.keepmyplanet.dto.zone.LocationQueryParams
import pt.isel.keepmyplanet.dto.zone.ReportZoneRequest
import pt.isel.keepmyplanet.dto.zone.UpdateSeverityRequest
import pt.isel.keepmyplanet.dto.zone.UpdateStatusRequest
import pt.isel.keepmyplanet.errors.ValidationException
import pt.isel.keepmyplanet.mapper.zone.toResponse
import pt.isel.keepmyplanet.service.ZoneService
import pt.isel.keepmyplanet.util.safeValueOf

fun getCurrentUserId(call: ApplicationCall): Id = Id(1U)

fun Route.zoneWebApi(zoneService: ZoneService) {
    route("/zones") {
        post {
            val request = call.receive<ReportZoneRequest>()
            val reporterId = getCurrentUserId(call)
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
            val params =
                LocationQueryParams(
                    lat = call.request.queryParameters["lat"]?.toDoubleOrNull(),
                    lon = call.request.queryParameters["lon"]?.toDoubleOrNull(),
                    radius = call.request.queryParameters["radius"]?.toDoubleOrNull(),
                )

            determineZoneResult(params, zoneService)
                .onSuccess { z -> call.respond(HttpStatusCode.OK, z.map { it.toResponse() }) }
                .onFailure { throw it }
        }

        route("/{id}") {
            fun ApplicationCall.getZoneIdFromPath(): Id {
                val idValue =
                    parameters["id"]?.toUIntOrNull()
                        ?: throw ValidationException("Zone ID must be a positive integer.")
                return Id(idValue)
            }

            get {
                val zoneId = call.getZoneIdFromPath()
                zoneService
                    .getZoneDetails(zoneId)
                    .onSuccess { zone -> call.respond(HttpStatusCode.OK, zone.toResponse()) }
                    .onFailure { throw it }
            }

            delete {
                val zoneId = call.getZoneIdFromPath()
                zoneService
                    .deleteZone(zoneId)
                    .onSuccess { call.respond(HttpStatusCode.NoContent) }
                    .onFailure { throw it }
            }

            patch("/status") {
                val zoneId = call.getZoneIdFromPath()
                val request = call.receive<UpdateStatusRequest>()
                val newStatus =
                    safeValueOf<ZoneStatus>(request.status)
                        ?: throw IllegalArgumentException("Invalid status value: ${request.status}")

                zoneService
                    .updateZoneStatus(zoneId, newStatus)
                    .onSuccess { call.respond(HttpStatusCode.OK, it.toResponse()) }
                    .onFailure { throw it }
            }

            patch("/severity") {
                val zoneId = call.getZoneIdFromPath()
                val request = call.receive<UpdateSeverityRequest>()
                val newZoneSeverity =
                    safeValueOf<ZoneSeverity>(request.severity)
                        ?: throw IllegalArgumentException("Invalid severity value: ${request.severity}")

                zoneService
                    .updateZoneSeverity(zoneId, newZoneSeverity)
                    .onSuccess { call.respond(HttpStatusCode.OK, it.toResponse()) }
                    .onFailure { throw it }
            }

            route("/photos") {
                fun ApplicationCall.getPhotoIdFromPath(): Id {
                    val idValue =
                        parameters["photoId"]?.toUIntOrNull()
                            ?: throw ValidationException("Photo ID must be a positive integer.")
                    return Id(idValue)
                }

                post {
                    val zoneId = call.getZoneIdFromPath()
                    val request = call.receive<AddPhotoRequest>()
                    val photoId = Id(request.photoId)

                    zoneService
                        .addPhotoToZone(zoneId, photoId)
                        .onSuccess { call.respond(HttpStatusCode.OK, it.toResponse()) }
                        .onFailure { throw it }
                }

                delete("/{photoId}") {
                    val zoneId = call.getZoneIdFromPath()
                    val photoId = call.getPhotoIdFromPath()

                    zoneService
                        .removePhotoFromZone(zoneId, photoId)
                        .onSuccess { call.respond(HttpStatusCode.OK, it.toResponse()) }
                        .onFailure { throw it }
                }
            }
        }
    }
}

private suspend fun determineZoneResult(
    params: LocationQueryParams,
    zoneService: ZoneService,
): Result<List<Zone>> {
    val lat = params.lat
    val lon = params.lon
    val radius = params.radius

    return when {
        lat != null && lon != null && radius != null -> {
            if (radius <= 0) {
                throw ValidationException("Query parameter 'radius' must be a positive number.")
            }
            zoneService.findZones(Location(lat, lon), radius)
        }

        lat == null && lon == null && radius == null -> {
            zoneService.findAll()
        }

        else -> throw ValidationException("If providing location, all parameters must be present.")
    }
}
