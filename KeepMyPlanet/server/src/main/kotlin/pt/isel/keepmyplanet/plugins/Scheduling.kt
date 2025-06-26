package pt.isel.keepmyplanet.plugins

import io.ktor.server.application.*
import kotlinx.coroutines.runBlocking
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import pt.isel.keepmyplanet.service.EventStateChangeService
import org.koin.ktor.ext.inject
import org.slf4j.LoggerFactory

fun Application.configureScheduling() {
    val eventStateChangeService by inject<EventStateChangeService>()
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
            }
        },
        0,
        1,
        TimeUnit.MINUTES,
    )
}
