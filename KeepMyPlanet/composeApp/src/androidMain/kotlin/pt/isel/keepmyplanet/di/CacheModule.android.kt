package pt.isel.keepmyplanet.di

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module
import pt.isel.keepmyplanet.cache.KeepMyPlanetCache

internal actual fun createDriverModule(): Module =
    module {
        single<SqlDriver> {
            AndroidSqliteDriver(KeepMyPlanetCache.Schema, androidContext(), "KeepMyPlanetCache.db")
        }
    }
