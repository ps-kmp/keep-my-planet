package pt.isel.keepmyplanet.util

import pt.isel.keepmyplanet.domain.common.Location
import kotlin.math.PI
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

fun calculateDistanceKm(
    loc1: Location,
    loc2: Location,
): Double {
    val earthRadiusKm = 6371.0

    val lat1Rad = loc1.latitude.toRadians()
    val lon1Rad = loc1.longitude.toRadians()
    val lat2Rad = loc2.latitude.toRadians()
    val lon2Rad = loc2.longitude.toRadians()

    val dLat = lat2Rad - lat1Rad
    val dLon = lon2Rad - lon1Rad

    val a = sin(dLat / 2).pow(2) + cos(lat1Rad) * cos(lat2Rad) * sin(dLon / 2).pow(2)
    val c = 2 * asin(sqrt(a))

    return earthRadiusKm * c
}

private fun Double.toRadians(): Double = this * PI / 180
