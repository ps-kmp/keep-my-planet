package pt.isel.keepmyplanet.di

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import java.io.File
import org.koin.core.module.Module
import org.koin.dsl.module
import pt.isel.keepmyplanet.cache.KeepMyPlanetCache

internal actual fun createDriverModule(): Module =
    module {
        single<SqlDriver> {
            val databasePath = File(System.getProperty("java.io.tmpdir"), "KeepMyPlanetCache.db")
            val driver = JdbcSqliteDriver("jdbc:sqlite:${databasePath.absolutePath}")
            if (!databasePath.exists()) {
                KeepMyPlanetCache.Schema.create(driver)
            }
            driver
        }
    }
