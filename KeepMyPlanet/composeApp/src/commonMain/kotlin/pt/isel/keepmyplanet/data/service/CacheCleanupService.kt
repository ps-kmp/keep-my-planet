package pt.isel.keepmyplanet.data.service

import kotlin.time.Duration.Companion.days
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import pt.isel.keepmyplanet.data.repository.EventCacheRepository
import pt.isel.keepmyplanet.data.repository.EventStatusHistoryCacheRepository
import pt.isel.keepmyplanet.data.repository.GeocodingCacheRepository
import pt.isel.keepmyplanet.data.repository.MapTileCacheRepository
import pt.isel.keepmyplanet.data.repository.MessageCacheRepository
import pt.isel.keepmyplanet.data.repository.PhotoCacheRepository
import pt.isel.keepmyplanet.data.repository.UserCacheRepository
import pt.isel.keepmyplanet.data.repository.UserStatsCacheRepository
import pt.isel.keepmyplanet.data.repository.ZoneCacheRepository
import pt.isel.keepmyplanet.utils.minus
import pt.isel.keepmyplanet.utils.now

class CacheCleanupService(
    private val eventCacheRepository: EventCacheRepository,
    private val mapTileCacheRepository: MapTileCacheRepository,
    private val photoCacheRepository: PhotoCacheRepository,
    private val userStatsCacheRepository: UserStatsCacheRepository,
    private val userCacheRepository: UserCacheRepository,
    private val messageCacheRepository: MessageCacheRepository,
    private val zoneCacheRepository: ZoneCacheRepository,
    private val geocodingCacheRepository: GeocodingCacheRepository,
    private val eventStatusHistoryCacheRepository: EventStatusHistoryCacheRepository,
) {
    companion object {
        private const val ONE_DAY_IN_SECONDS = 24 * 60 * 60L
        private const val SEVEN_DAYS_IN_SECONDS = 7 * 24 * 60 * 60L
        private const val THIRTY_DAYS_IN_SECONDS = 30 * 24 * 60 * 60L
    }

    fun performCleanup() {
        CoroutineScope(Dispatchers.Default).launch {
            eventCacheRepository.deleteExpiredEvents(SEVEN_DAYS_IN_SECONDS)
            mapTileCacheRepository.deleteExpiredTiles(THIRTY_DAYS_IN_SECONDS)
            photoCacheRepository.deleteExpiredPhotos(THIRTY_DAYS_IN_SECONDS)
            userStatsCacheRepository.deleteExpiredStats(SEVEN_DAYS_IN_SECONDS)
            userCacheRepository.deleteExpiredUsers(THIRTY_DAYS_IN_SECONDS)
            zoneCacheRepository.deleteExpiredZones(THIRTY_DAYS_IN_SECONDS)
            geocodingCacheRepository.deleteExpiredResults(ONE_DAY_IN_SECONDS)
            eventStatusHistoryCacheRepository.deleteExpiredHistory(SEVEN_DAYS_IN_SECONDS)

            val messageExpiration = now().minus(30.days)
            messageCacheRepository.deleteExpiredMessages(messageExpiration.toString())
        }
    }
}
