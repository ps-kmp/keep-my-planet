package pt.isel.keepmyplanet.ui.map

import kotlin.math.pow

object MapConfiguration {
    const val TILE_SIZE = 256
    const val MAX_ZOOM = 18
    val MAP_DIMENSION = TILE_SIZE * 2.0.pow(MAX_ZOOM - 1).toInt()

    const val DEFAULT_LAT = 38.7223
    const val DEFAULT_LON = -9.1393
    const val INITIAL_SCALE = 12.5

    const val USER_LOCATION_MARKER_ID = "user_location_marker"
    const val ZONE_CLUSTER_ID = "zone-clusterer"
}
