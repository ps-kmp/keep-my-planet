package pt.isel.keepmyplanet.ui.map.model

import kotlin.math.PI
import kotlin.math.atan
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.tan

fun lonToX(lon: Double): Double = (lon + 180.0) / 360.0

fun latToY(lat: Double): Double {
    val latRad = lat * PI / 180.0
    return (1.0 - ln(tan(latRad) + 1.0 / cos(latRad)) / PI) / 2.0
}

fun xToLon(x: Double): Double = x * 360.0 - 180.0

fun yToLat(y: Double): Double {
    val n = PI - 2.0 * PI * y
    return 180.0 / PI * atan(0.5 * (exp(n) - exp(-n)))
}
