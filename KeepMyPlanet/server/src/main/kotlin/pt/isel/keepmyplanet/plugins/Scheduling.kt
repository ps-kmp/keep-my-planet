package pt.isel.keepmyplanet.plugins

import io.ktor.server.application.Application
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.ktor.ext.inject
import org.slf4j.LoggerFactory
import pt.isel.keepmyplanet.service.EventStateChangeService
import pt.isel.keepmyplanet.service.ZoneService

fun Application.configureScheduling() {
    val eventStateChangeService by inject<EventStateChangeService>()
    val zoneService by inject<ZoneService>()
    val log = LoggerFactory.getLogger("EventSchedulingJob")
    val scheduler = Executors.newSingleThreadScheduledExecutor()
    val schedulerScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    scheduler.scheduleAtFixedRate(
        {
            schedulerScope.launch {
                try {
                    log.info("Running scheduled event and zone processing jobs...")
                    eventStateChangeService.transitionEventsToInProgress()
                    zoneService.processZoneConfirmationTimeouts()
                    log.info("Scheduled jobs finished successfully.")
                } catch (e: Exception) {
                    log.error("Error during scheduled jobs execution", e)
                }
            }
        },
        0,
        1,
        TimeUnit.MINUTES,
    )
}
