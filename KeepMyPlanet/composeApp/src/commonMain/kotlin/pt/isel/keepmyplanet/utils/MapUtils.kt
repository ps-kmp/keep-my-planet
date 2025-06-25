package pt.isel.keepmyplanet.utils

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.isSuccess
import kotlin.math.PI
import kotlin.math.atan
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan
import kotlinx.io.Buffer
import ovh.plrapps.mapcompose.core.TileStreamProvider

fun getTileStreamProvider(httpClient: HttpClient = HttpClient()) =
    TileStreamProvider { row, col, zoomLvl ->
        try {
            val response = httpClient.get("https://tile.openstreetmap.org/$zoomLvl/$col/$row.png")
            if (response.status.isSuccess()) {
                val bytes: ByteArray = response.body()
                Buffer().apply { write(bytes) }
            } else {
                null
            }
        } catch (e: Exception) {
            println("Error fetching tile: $e")
            null
        }
    }

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

fun haversineDistance(
    lat1: Double,
    lon1: Double,
    lat2: Double,
    lon2: Double,
): Double {
    val r = 6371e3
    val phi1 = lat1 * PI / 180.0
    val phi2 = lat2 * PI / 180.0
    val deltaPhi = (lat2 - lat1) * PI / 180.0
    val deltaLambda = (lon2 - lon1) * PI / 180.0

    val a = sin(deltaPhi / 2).pow(2) + cos(phi1) * cos(phi2) * sin(deltaLambda / 2).pow(2)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))

    return r * c
}
