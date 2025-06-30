package pt.isel.keepmyplanet.di

import org.koin.core.module.Module
import org.koin.dsl.module
import pt.isel.keepmyplanet.cache.KeepMyPlanetCache
import pt.isel.keepmyplanet.data.repository.EventCacheRepository
import pt.isel.keepmyplanet.data.repository.EventStatusHistoryCacheRepository
import pt.isel.keepmyplanet.data.repository.GeocodingCacheRepository
import pt.isel.keepmyplanet.data.repository.MapTileCacheRepository
import pt.isel.keepmyplanet.data.repository.MessageCacheRepository
import pt.isel.keepmyplanet.data.repository.OfflineReportQueueRepository
import pt.isel.keepmyplanet.data.repository.PhotoCacheRepository
import pt.isel.keepmyplanet.data.repository.UserCacheRepository
import pt.isel.keepmyplanet.data.repository.UserStatsCacheRepository
import pt.isel.keepmyplanet.data.repository.ZoneCacheRepository

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
