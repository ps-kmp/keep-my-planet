package pt.isel.keepmyplanet.data.api

import io.ktor.client.HttpClient

expect fun createHttpClient(): HttpClient
