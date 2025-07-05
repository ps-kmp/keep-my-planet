package pt.isel.keepmyplanet.data.service

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import pt.isel.keepmyplanet.data.cache.CleanableCache
import pt.isel.keepmyplanet.data.cache.EventCacheRepository
import pt.isel.keepmyplanet.data.cache.EventStatusHistoryCacheRepository
import pt.isel.keepmyplanet.data.cache.GeocodingCacheRepository
import pt.isel.keepmyplanet.data.cache.MessageCacheRepository

class CacheCleanupService(
    private val cleanableCaches: List<CleanableCache>,
) {
    companion object {
        private const val ONE_DAY_IN_SECONDS = 24 * 60 * 60L
        private const val SEVEN_DAYS_IN_SECONDS = 7 * 24 * 60 * 60L
        private const val THIRTY_DAYS_IN_SECONDS = 30 * 24 * 60 * 60L
    }

    fun performCleanup() {
        CoroutineScope(Dispatchers.Default).launch {
            cleanableCaches.forEach { cache ->
                val ttl =
                    when (cache) {
                        is GeocodingCacheRepository -> ONE_DAY_IN_SECONDS
                        is EventCacheRepository, is EventStatusHistoryCacheRepository ->
                            SEVEN_DAYS_IN_SECONDS

                        is MessageCacheRepository -> THIRTY_DAYS_IN_SECONDS
                        else -> THIRTY_DAYS_IN_SECONDS
                    }
                cache.cleanupExpiredData(ttl)
            }
        }
    }
}
