package pt.isel.keepmyplanet.data.repository

import pt.isel.keepmyplanet.data.api.AuthApi
import pt.isel.keepmyplanet.dto.auth.LoginRequest
import pt.isel.keepmyplanet.dto.auth.LoginResponse

class AuthApiRepository(
    private val authApi: AuthApi,
) {
    suspend fun login(request: LoginRequest): Result<LoginResponse> = authApi.login(request)
}
