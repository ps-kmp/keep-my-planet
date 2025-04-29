package pt.isel.keepmyplanet.data.api

import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.cio.CIO

actual fun httpClientEngine(): HttpClientEngineFactory<*> = CIO
