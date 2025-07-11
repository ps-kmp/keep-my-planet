package pt.isel.keepmyplanet.di

import com.russhwolf.settings.Settings
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
import pt.isel.keepmyplanet.data.repository.AuthApiRepository
import pt.isel.keepmyplanet.data.repository.DeviceApiRepository
import pt.isel.keepmyplanet.data.repository.EventApiRepository
import pt.isel.keepmyplanet.data.repository.GeocodingApiRepository
import pt.isel.keepmyplanet.data.repository.MessageApiRepository
import pt.isel.keepmyplanet.data.repository.PhotoApiRepository
import pt.isel.keepmyplanet.data.repository.UserApiRepository
import pt.isel.keepmyplanet.data.repository.ZoneApiRepository
import pt.isel.keepmyplanet.data.service.CacheCleanupService
import pt.isel.keepmyplanet.session.SessionManager
import pt.isel.keepmyplanet.ui.admin.UserListViewModel
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
        // Session & Settings
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
        factory { Settings() }

        // Network
        factory { createHttpClient(get()) }
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
        factory { GeocodingApi(get(), getOrNull()) }
    }

private val repositoryModule =
    module {
        factoryOf(::AuthApiRepository)
        factoryOf(::DeviceApiRepository)
        factory {
            EventApiRepository(
                get(),
                getOrNull(),
                getOrNull(),
                getOrNull(),
                getOrNull(),
            )
        }
        factoryOf(::GeocodingApiRepository)
        factory { MessageApiRepository(get(), getOrNull()) }
        factory { PhotoApiRepository(get(), getOrNull(), get()) }
        factory { UserApiRepository(get(), getOrNull(), getOrNull()) }
        factory { ZoneApiRepository(get(), getOrNull(), get()) }
    }

private val viewModelModule =
    module {
        single { AppViewModel(get(), get()) }
        factoryOf(::LoginViewModel)
        factoryOf(::RegisterViewModel)
        factory { HomeViewModel(get(), get(), get(), get(), getOrNull()) }
        factory { EventListViewModel(get(), get()) }
        factory { EventDetailsViewModel(get(), get(), get()) }
        factoryOf(::EventStatusHistoryViewModel)
        factory { EventFormViewModel(get(), get()) }
        factory { MapViewModel(get(), get(), get(), get(), get()) }
        factoryOf(::ZoneDetailsViewModel)
        factoryOf(::UpdateZoneViewModel)
        factoryOf(::ReportZoneViewModel)
        factoryOf(::UserProfileViewModel)
        factoryOf(::ChatViewModel)
        factoryOf(::ManageAttendanceViewModel)
        factoryOf(::EventStatsViewModel)
        factoryOf(::UserStatsViewModel)
        factoryOf(::ParticipantListViewModel)
        factoryOf(::UserListViewModel)
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
