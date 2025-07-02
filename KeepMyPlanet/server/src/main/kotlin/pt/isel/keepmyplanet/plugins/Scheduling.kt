package pt.isel.keepmyplanet.plugins

import io.ktor.server.application.Application
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.runBlocking
import org.koin.ktor.ext.inject
import org.slf4j.LoggerFactory
import pt.isel.keepmyplanet.service.EventStateChangeService
import pt.isel.keepmyplanet.service.ZoneService

fun Application.configureScheduling() {
    val eventStateChangeService by inject<EventStateChangeService>()
    val zoneService by inject<ZoneService>()
    val log = LoggerFactory.getLogger("EventSchedulingJob")
    val scheduler = Executors.newSingleThreadScheduledExecutor()

    scheduler.scheduleAtFixedRate(
        {
            runBlocking {
                try {
                    log.info("Running event transition job...")
                    eventStateChangeService.transitionEventsToInProgress()
                    log.info("Event transition job finished.")
                } catch (e: Exception) {
                    log.error("Error in event transition job", e)
                }

                try {
                    log.info("Running zone confirmation timeout job...")
                    zoneService.processZoneConfirmationTimeouts()
                    log.info("Zone confirmation timeout job finished.")
                } catch (e: Exception) {
                    log.error("Error in zone confirmation timeout job", e)
                }
            }
        },
        0,
        1,
        TimeUnit.MINUTES, //ALTERAR PARA HORA A HORA OU DIA A DIA
    )
}
