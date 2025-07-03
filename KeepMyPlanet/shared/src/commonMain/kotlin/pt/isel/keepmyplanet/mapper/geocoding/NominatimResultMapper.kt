package pt.isel.keepmyplanet.mapper.geocoding

import pt.isel.keepmyplanet.domain.common.Place
import pt.isel.keepmyplanet.dto.geocoding.NominatimResult

fun NominatimResult.toPlace(): Place =
    Place(
        id = this.placeId,
        displayName = this.displayName,
        latitude = this.lat.toDouble(),
        longitude = this.lon.toDouble(),
    )
