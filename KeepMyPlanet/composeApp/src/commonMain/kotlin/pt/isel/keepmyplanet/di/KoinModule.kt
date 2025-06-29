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
        single { GeocodingApi(get()) }

        // ViewModels
        single { AppViewModel(get()) }
        factoryOf(::LoginViewModel)
        factoryOf(::RegisterViewModel)
        single { EventListViewModel(get()) }
        factoryOf(::EventDetailsViewModel)
        factoryOf(::EventStatusHistoryViewModel)
        factoryOf(::EventFormViewModel)
        factory { MapViewModel(get(), get()) }
        factoryOf(::ZoneDetailsViewModel)
        factoryOf(::UpdateZoneViewModel)
        factoryOf(::ReportZoneViewModel)
        factoryOf(::UserProfileViewModel)
        factoryOf(::ChatViewModel)
        factoryOf(::ManageAttendanceViewModel)
        factoryOf(::UserStatsViewModel)
    }

fun initKoin() {
    startKoin {
        modules(appModule)
    }
}
