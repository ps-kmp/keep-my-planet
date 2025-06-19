package pt.isel.keepmyplanet.ui.map.model

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.isSuccess
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
