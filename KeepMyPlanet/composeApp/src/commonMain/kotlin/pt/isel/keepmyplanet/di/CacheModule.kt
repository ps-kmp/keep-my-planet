package pt.isel.keepmyplanet.di

import org.koin.core.module.Module
import org.koin.dsl.module
import pt.isel.keepmyplanet.cache.KeepMyPlanetCache
import pt.isel.keepmyplanet.data.cache.EventCacheRepository
import pt.isel.keepmyplanet.data.cache.EventStatusHistoryCacheRepository
import pt.isel.keepmyplanet.data.cache.GeocodingCacheRepository
import pt.isel.keepmyplanet.data.cache.MapTileCacheRepository
import pt.isel.keepmyplanet.data.cache.MessageCacheRepository
import pt.isel.keepmyplanet.data.cache.OfflineReportQueueRepository
import pt.isel.keepmyplanet.data.cache.PhotoCacheRepository
import pt.isel.keepmyplanet.data.cache.UserCacheRepository
import pt.isel.keepmyplanet.data.cache.UserStatsCacheRepository
import pt.isel.keepmyplanet.data.cache.ZoneCacheRepository

internal expect fun createDriverModule(): Module

val cacheModule =
    module {
        includes(createDriverModule())
        single { KeepMyPlanetCache(get()) }
        single { MapTileCacheRepository(get()) }
        single { ZoneCacheRepository(get()) }
        single { OfflineReportQueueRepository(get()) }
        single { EventCacheRepository(get()) }
        single { MessageCacheRepository(get()) }
        single { UserCacheRepository(get()) }
        single { PhotoCacheRepository(get()) }
        single { UserStatsCacheRepository(get()) }
        single { GeocodingCacheRepository(get()) }
        single { EventStatusHistoryCacheRepository(get()) }
    }
