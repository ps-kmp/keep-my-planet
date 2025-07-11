package pt.isel.keepmyplanet.data.repository

import pt.isel.keepmyplanet.data.api.DeviceApi

class DeviceApiRepository(
    private val deviceApi: DeviceApi,
) {
    suspend fun registerDevice(
        token: String,
        platform: String,
    ): Result<Unit> = deviceApi.registerDevice(token, platform)
}
