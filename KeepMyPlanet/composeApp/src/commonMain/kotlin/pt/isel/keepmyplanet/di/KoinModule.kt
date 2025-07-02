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
import pt.isel.keepmyplanet.ui.login.LoginViewModel
import pt.isel.keepmyplanet.ui.map.MapViewModel
import pt.isel.keepmyplanet.ui.profile.UserProfileViewModel
import pt.isel.keepmyplanet.ui.register.RegisterViewModel
import pt.isel.keepmyplanet.ui.report.ReportZoneViewModel
import pt.isel.keepmyplanet.ui.stats.UserStatsViewModel
import pt.isel.keepmyplanet.ui.zone.details.ZoneDetailsViewModel
import pt.isel.keepmyplanet.ui.zone.update.UpdateZoneViewModel

val appModule =
    module {
        // Session
        single { ConnectivityService(get()) }
        single { SyncService(get(), get(), get(), get()) }
        single {
            CacheCleanupService(get(), get(), get(), get(), get(), get(), get(), get(), get())
        }

        single { SessionManager() }

        // Network
        single { createHttpClient(get()) }

        // APIs
        single { AuthApi(get()) }
        single { UserApi(get()) }
        single { EventApi(get()) }
        single { ChatApi(get()) }
        single { ZoneApi(get()) }
        single { PhotoApi(get()) }
        single { DeviceApi(get()) }
        single { GeocodingApi(get(), get()) }

        // Repositories
        single { DefaultAuthRepository(get()) }
        single { DefaultDeviceRepository(get()) }
        single { DefaultEventRepository(get(), get(), get(), get()) }
        single { DefaultGeocodingRepository(get()) }
        single { DefaultMessageRepository(get(), get()) }
        single { DefaultPhotoRepository(get(), get(), get()) }
        single { DefaultUserRepository(get(), get(), get()) }
        single { DefaultZoneRepository(get(), get()) }

        // ViewModels
        single { AppViewModel(get()) }
        factoryOf(::LoginViewModel)
        factoryOf(::RegisterViewModel)
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
        factory { params -> UserStatsViewModel(get(), get(), params.get()) }
    }

fun initKoin() {
    startKoin {
        modules(appModule, cacheModule)
    }
}
