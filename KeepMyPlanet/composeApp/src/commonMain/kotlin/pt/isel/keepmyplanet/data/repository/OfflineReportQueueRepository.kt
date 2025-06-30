package pt.isel.keepmyplanet.data.repository

import io.ktor.util.decodeBase64String
import io.ktor.util.encodeBase64
import io.ktor.utils.io.core.toByteArray
import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import pt.isel.keepmyplanet.cache.KeepMyPlanetCache
import pt.isel.keepmyplanet.domain.zone.ZoneSeverity
import pt.isel.keepmyplanet.ui.report.states.SelectedImage
import pt.isel.keepmyplanet.utils.safeValueOf

@Serializable
private data class QueuedPhoto(
    val filename: String,
    val dataBase64: String,
)

data class QueuedReport(
    val id: Long,
    val latitude: Double,
    val longitude: Double,
    val description: String,
    val severity: ZoneSeverity,
    val photos: List<SelectedImage>,
    val retries: Long,
)

class OfflineReportQueueRepository(
    database: KeepMyPlanetCache,
) {
    private val queries = database.offlineReportQueueQueries

    suspend fun queueReport(
        latitude: Double,
        longitude: Double,
        description: String,
        severity: ZoneSeverity,
        photos: List<SelectedImage>,
    ) {
        val photosJson =
            Json.encodeToString(
                photos.map { QueuedPhoto(it.filename, it.data.encodeBase64()) },
            )

        queries.queueReport(
            latitude = latitude,
            longitude = longitude,
            description = description,
            severity = severity.name,
            photos_json = photosJson,
            timestamp = Clock.System.now().epochSeconds,
        )
    }

    fun peekNextReport(): QueuedReport? {
        val dbReport = queries.peekNextReport().executeAsOneOrNull() ?: return null
        val photos =
            Json.decodeFromString<List<QueuedPhoto>>(dbReport.photos_json).map {
                SelectedImage(it.dataBase64.decodeBase64String().toByteArray(), it.filename)
            }
        return QueuedReport(
            id = dbReport.id,
            latitude = dbReport.latitude,
            longitude = dbReport.longitude,
            description = dbReport.description,
            severity = safeValueOf<ZoneSeverity>(dbReport.severity) ?: ZoneSeverity.UNKNOWN,
            photos = photos,
            retries = dbReport.retries,
        )
    }

    suspend fun deleteReport(id: Long) {
        queries.deleteReport(id)
    }

    suspend fun incrementReportRetries(id: Long) {
        queries.incrementReportRetries(id)
    }
}
