package pt.isel.keepmyplanet.data.service

import io.ktor.client.HttpClient
import io.ktor.client.request.head
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class ConnectivityService(
    private val httpClient: HttpClient,
) {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private val _isOnline = MutableStateFlow(true)
    val isOnline = _isOnline.asStateFlow()

    init {
        scope.launch {
            while (isActive) {
                _isOnline.value = checkConnectivity()
                delay(5000)
            }
        }
    }

    private suspend fun checkConnectivity(): Boolean =
        runCatching { httpClient.head("https://www.google.com/generate_204") }.isSuccess

    fun shutdown() {
        scope.cancel()
    }
}
