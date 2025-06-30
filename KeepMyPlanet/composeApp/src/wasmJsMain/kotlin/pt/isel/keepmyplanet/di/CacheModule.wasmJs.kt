package pt.isel.keepmyplanet.di

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.worker.WebWorkerDriver
import org.koin.core.module.Module
import org.koin.dsl.module
import org.w3c.dom.Worker
import pt.isel.keepmyplanet.cache.KeepMyPlanetCache

private fun createWorkerUrl(): String =
    js("""new URL("@cashapp/sqldelight-sqljs-worker/sqljs.worker.js", import.meta.url).href""")

internal actual fun createDriverModule(): Module =
    module {
        single<SqlDriver> {
            val driver = WebWorkerDriver(Worker(createWorkerUrl()))
            KeepMyPlanetCache.Schema.create(driver)
            driver
        }
    }
