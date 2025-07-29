package pt.isel.keepmyplanet.api

import io.ktor.server.plugins.origin
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import pt.isel.keepmyplanet.service.IpGeocodingService

fun Route.ipGeocodingWebApi(service: IpGeocodingService) {
    route("/geocoding") {
        get("/ip-location") {
            val clientIp = call.request.origin.remoteHost
            service
                .getIpBasedLocation(clientIp)
                .onSuccess { call.respond(it) }
                .onFailure { throw it }
        }
    }
}
