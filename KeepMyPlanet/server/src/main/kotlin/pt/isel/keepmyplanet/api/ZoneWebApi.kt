package pt.isel.keepmyplanet.api

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.plugins.BadRequestException
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
import pt.isel.keepmyplanet.domain.zone.Severity
import pt.isel.keepmyplanet.domain.zone.Zone
import pt.isel.keepmyplanet.domain.zone.ZoneStatus
import pt.isel.keepmyplanet.dto.zone.AddPhotoRequest
import pt.isel.keepmyplanet.dto.zone.ReportZoneRequest
import pt.isel.keepmyplanet.dto.zone.UpdateSeverityRequest
import pt.isel.keepmyplanet.dto.zone.UpdateStatusRequest
import pt.isel.keepmyplanet.mapper.zone.toResponse
import pt.isel.keepmyplanet.services.ZoneService
import pt.isel.keepmyplanet.util.safeValueOf

fun getCurrentUserId(call: ApplicationCall): Id = Id(1U)

fun Route.zoneWebApi(zoneService: ZoneService) {
    route("/zones") {
        post {
            val request = call.receive<ReportZoneRequest>()
            val reporterId = getCurrentUserId(call)
            val location = Location(request.latitude, request.longitude)
            val description = Description(request.description)
            val severity = safeValueOf<Severity>(request.severity) ?: Severity.UNKNOWN
            val photoIds = request.photoIds.map { Id(it) }.toSet()

            zoneService
                .reportZone(location, description, photoIds, reporterId, severity)
                .onSuccess { call.respond(HttpStatusCode.Created, it.toResponse()) }
                .onFailure { throw it }
        }

        get {
            val lat = call.request.queryParameters["lat"]?.toDoubleOrNull()
            val lon = call.request.queryParameters["lon"]?.toDoubleOrNull()
            val radius = call.request.queryParameters["radius"]?.toDoubleOrNull()

            val zonesResult: Result<List<Zone>> =
                when {
                    lat != null && lon != null && radius != null -> {
                        if (radius <= 0) throw BadRequestException("Radius must be positive")
                        zoneService.findZones(Location(lat, lon), radius)
                    }

                    lat != null || lon != null || radius != null -> {
                        throw BadRequestException(
                            "All location parameters must be provided together",
                        )
                    }

                    else -> zoneService.findAll()
                }

            zonesResult
                .onSuccess { z -> call.respond(HttpStatusCode.OK, z.map { it.toResponse() }) }
                .onFailure { throw it }
        }

        route("/{id}") {
            fun ApplicationCall.getZoneIdFromPath(): Id {
                val idValue =
                    parameters["id"]?.toUIntOrNull()
                        ?: throw NumberFormatException("Zone ID must be a positive integer.")
                try {
                    return Id(idValue)
                } catch (e: IllegalArgumentException) {
                    throw BadRequestException(e.message ?: "Invalid Zone ID format.")
                }
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
                val newSeverity =
                    safeValueOf<Severity>(request.severity)
                        ?: throw IllegalArgumentException("Invalid severity value: ${request.severity}")

                zoneService
                    .updateZoneSeverity(zoneId, newSeverity)
                    .onSuccess { call.respond(HttpStatusCode.OK, it.toResponse()) }
                    .onFailure { throw it }
            }

            route("/photos") {
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
                    val photoIdValue =
                        call.parameters["photoId"]?.toUIntOrNull()
                            ?: throw NumberFormatException("Photo ID must be a positive integer.")
                    val photoId = Id(photoIdValue)

                    zoneService
                        .removePhotoFromZone(zoneId, photoId)
                        .onSuccess { call.respond(HttpStatusCode.OK, it.toResponse()) }
                        .onFailure { throw it }
                }
            }
        }
    }
}
