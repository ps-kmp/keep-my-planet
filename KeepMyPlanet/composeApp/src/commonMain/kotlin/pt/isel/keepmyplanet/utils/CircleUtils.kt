package pt.isel.keepmyplanet.utils

import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import ovh.plrapps.mapcompose.ui.paths.PathDataBuilder

fun PathDataBuilder.addCircle(
    centerLat: Double,
    centerLon: Double,
    radiusMeters: Double,
) {
    val earthRadiusMeters = 6371000.0
    val d = radiusMeters / earthRadiusMeters
    val lat1 = centerLat.toRadians()
    val lon1 = centerLon.toRadians()

    for (i in 0..360 step 5) {
        val brng = i.toDouble().toRadians()
        val lat2 = asin(sin(lat1) * cos(d) + cos(lat1) * sin(d) * cos(brng))
        val lon2 = lon1 + atan2(sin(brng) * sin(d) * cos(lat1), cos(d) - sin(lat1) * sin(lat2))

        addPoint(lonToX(lon2.toDegrees()), latToY(lat2.toDegrees()))
    }
}
