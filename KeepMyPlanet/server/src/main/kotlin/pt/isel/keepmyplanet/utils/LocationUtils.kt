package pt.isel.keepmyplanet.utils

import kotlin.math.PI
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import pt.isel.keepmyplanet.domain.zone.Location

private const val EARTH_RADIUS_KM = 6371.0

fun calculateDistanceKm(
    loc1: Location,
    loc2: Location,
): Double {
    val lat1Rad = loc1.latitude.toRadians()
    val lon1Rad = loc1.longitude.toRadians()
    val lat2Rad = loc2.latitude.toRadians()
    val lon2Rad = loc2.longitude.toRadians()

    val dLat = lat2Rad - lat1Rad
    val dLon = lon2Rad - lon1Rad

    val a = sin(dLat / 2).pow(2) + cos(lat1Rad) * cos(lat2Rad) * sin(dLon / 2).pow(2)
    val c = 2 * asin(sqrt(a))

    return EARTH_RADIUS_KM * c
}

fun calculateBoundingBox(
    center: Location,
    radiusKm: Double,
): Pair<Location, Location> {
    val latRad = center.latitude.toRadians()

    val deltaLat = radiusKm / EARTH_RADIUS_KM
    val deltaLon = radiusKm / (EARTH_RADIUS_KM * cos(latRad))

    val minLat = center.latitude - deltaLat.toDegrees()
    val maxLat = center.latitude + deltaLat.toDegrees()
    val minLon = center.longitude - deltaLon.toDegrees()
    val maxLon = center.longitude + deltaLon.toDegrees()

    return Pair(Location(minLat, minLon), Location(maxLat, maxLon))
}

private fun Double.toRadians(): Double = this * PI / 180

private fun Double.toDegrees(): Double = this * 180 / PI
