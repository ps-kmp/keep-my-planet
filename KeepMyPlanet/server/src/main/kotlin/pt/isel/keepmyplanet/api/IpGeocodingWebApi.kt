package pt.isel.keepmyplanet.api

import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import pt.isel.keepmyplanet.service.IpGeocodingService

fun Route.ipGeocodingWebApi(service: IpGeocodingService) {
    route("/geocoding") {
        get("/ip-location") {
            service
                .getIpBasedLocation()
                .onSuccess { call.respond(it) }
                .onFailure { throw it }
        }
    }
}
