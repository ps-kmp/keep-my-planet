package pt.isel.keepmyplanet.data.cache

import io.ktor.util.decodeBase64String
import io.ktor.util.encodeBase64
import io.ktor.utils.io.core.toByteArray
import kotlinx.datetime.Clock
import kotlinx.serialization.json.Json
import pt.isel.keepmyplanet.cache.KeepMyPlanetCache
import pt.isel.keepmyplanet.data.cache.models.QueuedAction
import pt.isel.keepmyplanet.data.cache.models.QueuedReport
import pt.isel.keepmyplanet.domain.zone.ZoneSeverity
import pt.isel.keepmyplanet.ui.report.states.SelectedImage
import pt.isel.keepmyplanet.utils.safeValueOf

class OfflineReportQueueRepository(
    database: KeepMyPlanetCache,
) {
    private val queries = database.offlineReportQueueQueries

    suspend fun queueReport(
        latitude: Double,
        longitude: Double,
        radius: Double,
        description: String,
        severity: ZoneSeverity,
        photos: List<SelectedImage>,
    ) {
        val photosJson =
            Json.encodeToString(photos.map { QueuedAction(it.filename, it.data.encodeBase64()) })

        queries.queueReport(
            latitude = latitude,
            longitude = longitude,
            radius = radius,
            description = description,
            severity = severity.name,
            photos_json = photosJson,
            timestamp = Clock.System.now().epochSeconds,
        )
    }

    suspend fun peekNextReport(): QueuedReport? {
        val dbReport = queries.peekNextReport().executeAsOneOrNull() ?: return null
        val photos =
            Json.decodeFromString<List<QueuedAction>>(dbReport.photos_json).map {
                SelectedImage(it.dataBase64.decodeBase64String().toByteArray(), it.filename)
            }
        return QueuedReport(
            id = dbReport.id,
            latitude = dbReport.latitude,
            longitude = dbReport.longitude,
            radius = dbReport.radius,
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
