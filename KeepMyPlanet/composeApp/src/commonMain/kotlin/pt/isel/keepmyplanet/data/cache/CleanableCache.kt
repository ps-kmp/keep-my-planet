package pt.isel.keepmyplanet.data.cache

interface CleanableCache {
    suspend fun cleanupExpiredData(ttlSeconds: Long)
}
