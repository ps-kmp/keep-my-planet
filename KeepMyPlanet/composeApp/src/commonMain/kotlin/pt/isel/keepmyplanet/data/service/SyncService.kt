package pt.isel.keepmyplanet.data.service

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import pt.isel.keepmyplanet.data.api.PhotoApi
import pt.isel.keepmyplanet.data.api.ZoneApi
import pt.isel.keepmyplanet.data.cache.OfflineReportQueueRepository
import pt.isel.keepmyplanet.dto.zone.ReportZoneRequest

private const val MAX_RETRIES = 3

class SyncService(
    private val connectivityService: ConnectivityService,
    private val offlineReportQueueRepository: OfflineReportQueueRepository,
    private val zoneApi: ZoneApi,
    private val photoApi: PhotoApi,
) {
    private val coroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    fun start() {
        coroutineScope.launch {
            connectivityService.isOnline
                .filter { it }
                .collect { processQueue() }
        }
    }

    private suspend fun processQueue() {
        while (true) {
            val reportToSync = offlineReportQueueRepository.peekNextReport() ?: break

            if (reportToSync.retries >= MAX_RETRIES) {
                println(
                    "Report ${reportToSync.id} failed permanently " +
                        "after ${reportToSync.retries} retries.",
                )
                offlineReportQueueRepository.deleteReport(reportToSync.id)
                continue
            }

            val success =
                runCatching {
                    val photoIds =
                        reportToSync.photos
                            .map { photoApi.createPhoto(it.data, it.filename).getOrThrow().id }
                            .toSet()

                    val reportRequest =
                        ReportZoneRequest(
                            latitude = reportToSync.latitude,
                            longitude = reportToSync.longitude,
                            description = reportToSync.description,
                            severity = reportToSync.severity.name,
                            photoIds = photoIds,
                        )
                    zoneApi.reportZone(reportRequest).getOrThrow()
                }.isSuccess

            if (success) {
                offlineReportQueueRepository.deleteReport(reportToSync.id)
            } else {
                offlineReportQueueRepository.incrementReportRetries(reportToSync.id)
            }
        }
    }

    fun shutdown() {
        coroutineScope.cancel()
    }
}
