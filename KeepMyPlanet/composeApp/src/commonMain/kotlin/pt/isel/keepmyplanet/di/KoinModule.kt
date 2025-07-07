package pt.isel.keepmyplanet.di

import org.koin.core.context.startKoin
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module
import pt.isel.keepmyplanet.AppViewModel
import pt.isel.keepmyplanet.data.api.AuthApi
import pt.isel.keepmyplanet.data.api.ChatApi
import pt.isel.keepmyplanet.data.api.DeviceApi
import pt.isel.keepmyplanet.data.api.EventApi
import pt.isel.keepmyplanet.data.api.GeocodingApi
import pt.isel.keepmyplanet.data.api.PhotoApi
import pt.isel.keepmyplanet.data.api.UserApi
import pt.isel.keepmyplanet.data.api.ZoneApi
import pt.isel.keepmyplanet.data.cache.CleanableCache
import pt.isel.keepmyplanet.data.cache.EventCacheRepository
import pt.isel.keepmyplanet.data.cache.EventStatsCacheRepository
import pt.isel.keepmyplanet.data.cache.EventStatusHistoryCacheRepository
import pt.isel.keepmyplanet.data.cache.GeocodingCacheRepository
import pt.isel.keepmyplanet.data.cache.MapTileCacheRepository
import pt.isel.keepmyplanet.data.cache.MessageCacheRepository
import pt.isel.keepmyplanet.data.cache.PhotoCacheRepository
import pt.isel.keepmyplanet.data.cache.UserCacheRepository
import pt.isel.keepmyplanet.data.cache.UserStatsCacheRepository
import pt.isel.keepmyplanet.data.cache.ZoneCacheRepository
import pt.isel.keepmyplanet.data.http.createHttpClient
import pt.isel.keepmyplanet.data.repository.DefaultAuthRepository
import pt.isel.keepmyplanet.data.repository.DefaultDeviceRepository
import pt.isel.keepmyplanet.data.repository.DefaultEventRepository
import pt.isel.keepmyplanet.data.repository.DefaultGeocodingRepository
import pt.isel.keepmyplanet.data.repository.DefaultMessageRepository
import pt.isel.keepmyplanet.data.repository.DefaultPhotoRepository
import pt.isel.keepmyplanet.data.repository.DefaultUserRepository
import pt.isel.keepmyplanet.data.repository.DefaultZoneRepository
import pt.isel.keepmyplanet.data.service.CacheCleanupService
import pt.isel.keepmyplanet.data.service.ConnectivityService
import pt.isel.keepmyplanet.data.service.SyncService
import pt.isel.keepmyplanet.session.SessionManager
import pt.isel.keepmyplanet.ui.attendance.ManageAttendanceViewModel
import pt.isel.keepmyplanet.ui.chat.ChatViewModel
import pt.isel.keepmyplanet.ui.event.details.EventDetailsViewModel
import pt.isel.keepmyplanet.ui.event.forms.EventFormViewModel
import pt.isel.keepmyplanet.ui.event.history.EventStatusHistoryViewModel
import pt.isel.keepmyplanet.ui.event.list.EventListViewModel
import pt.isel.keepmyplanet.ui.event.participants.ParticipantListViewModel
import pt.isel.keepmyplanet.ui.event.stats.EventStatsViewModel
import pt.isel.keepmyplanet.ui.home.HomeViewModel
import pt.isel.keepmyplanet.ui.login.LoginViewModel
import pt.isel.keepmyplanet.ui.map.MapViewModel
import pt.isel.keepmyplanet.ui.profile.UserProfileViewModel
import pt.isel.keepmyplanet.ui.register.RegisterViewModel
import pt.isel.keepmyplanet.ui.report.ReportZoneViewModel
import pt.isel.keepmyplanet.ui.stats.UserStatsViewModel
import pt.isel.keepmyplanet.ui.zone.details.ZoneDetailsViewModel
import pt.isel.keepmyplanet.ui.zone.update.UpdateZoneViewModel

private val serviceModule =
    module {
        // Session
        single { ConnectivityService(get()) }
        single { SyncService(get(), get(), get(), get()) }
        single<List<CleanableCache>> {
            listOf(
                get<EventCacheRepository>(),
                get<MapTileCacheRepository>(),
                get<PhotoCacheRepository>(),
                get<UserStatsCacheRepository>(),
                get<UserCacheRepository>(),
                get<MessageCacheRepository>(),
                get<ZoneCacheRepository>(),
                get<GeocodingCacheRepository>(),
                get<EventStatusHistoryCacheRepository>(),
                get<EventStatsCacheRepository>(),
            )
        }
        single { CacheCleanupService(get()) }

        single { SessionManager() }

        // Network
        single { createHttpClient(get()) }
    }

private val apiModule =
    module {
        factoryOf(::AuthApi)
        factoryOf(::UserApi)
        factoryOf(::EventApi)
        factoryOf(::ChatApi)
        factoryOf(::ZoneApi)
        factoryOf(::PhotoApi)
        factoryOf(::DeviceApi)
        single { GeocodingApi(get(), getOrNull()) }
    }

private val repositoryModule =
    module {
        factoryOf(::DefaultAuthRepository)
        factoryOf(::DefaultDeviceRepository)
        single { DefaultEventRepository(get(), getOrNull(), getOrNull(), getOrNull(), getOrNull()) }
        factoryOf(::DefaultGeocodingRepository)
        single { DefaultMessageRepository(get(), getOrNull()) }
        single { DefaultPhotoRepository(get(), getOrNull(), get()) }
        single { DefaultUserRepository(get(), getOrNull(), getOrNull()) }
        single { DefaultZoneRepository(get(), getOrNull(), get()) }
    }

private val viewModelModule =
    module {
        single { AppViewModel(get(), get()) }
        factoryOf(::LoginViewModel)
        factoryOf(::RegisterViewModel)
        factory { HomeViewModel(get(), get(), get(), getOrNull(), getOrNull()) }
        factoryOf(::EventListViewModel)
        factory { EventDetailsViewModel(get(), get(), get()) }
        factoryOf(::EventStatusHistoryViewModel)
        factory { EventFormViewModel(get(), get()) }
        factoryOf(::MapViewModel)
        factoryOf(::ZoneDetailsViewModel)
        factoryOf(::UpdateZoneViewModel)
        factoryOf(::ReportZoneViewModel)
        factoryOf(::UserProfileViewModel)
        factory { params -> ChatViewModel(get(), get(), get(), params.get()) }
        factoryOf(::ManageAttendanceViewModel)
        factory { params -> EventStatsViewModel(get(), params.get()) }
        factory { params -> UserStatsViewModel(get(), get(), params.get()) }
        factory { params -> ParticipantListViewModel(params.get(), get()) }
    }

val appModule =
    module {
        includes(serviceModule, apiModule, repositoryModule, viewModelModule, cacheModule)
    }

fun initKoin() {
    startKoin {
        modules(appModule)
    }
}
