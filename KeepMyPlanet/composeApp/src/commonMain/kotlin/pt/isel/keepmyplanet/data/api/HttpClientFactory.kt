@file:Suppress("ktlint:standard:no-empty-file", "ktlint:standard:no-wildcard-imports")

package pt.isel.keepmyplanet.data.api

import io.ktor.client.*

expect fun createHttpClient(): HttpClient
