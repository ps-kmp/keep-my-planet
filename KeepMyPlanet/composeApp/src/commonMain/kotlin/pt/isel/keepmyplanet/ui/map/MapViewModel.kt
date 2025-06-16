package pt.isel.keepmyplanet.ui.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pt.isel.keepmyplanet.data.api.ZoneApi
import pt.isel.keepmyplanet.data.http.ApiException
import pt.isel.keepmyplanet.mapper.zone.toZone
import pt.isel.keepmyplanet.ui.map.model.MapScreenEvent
import pt.isel.keepmyplanet.ui.map.model.MapUiState

class MapViewModel(
    private val zoneApi: ZoneApi,
) : ViewModel() {
    private val _uiState = MutableStateFlow(MapUiState())
    val uiState = _uiState.asStateFlow()

    private val _events = Channel<MapScreenEvent>()
    val events = _events.receiveAsFlow()

    init {
        loadZones()
    }

    fun loadZones() {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            zoneApi
                .findAllZones()
                .onSuccess { zonesResponse ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            zones = zonesResponse.map { response -> response.toZone() },
                        )
                    }
                }.onFailure { error ->
                    _uiState.update { it.copy(isLoading = false) }
                    handleError("Failed to load zones", error)
                }
        }
    }

    fun onZoneSelected(zoneId: UInt) {
        viewModelScope.launch {
            _events.send(MapScreenEvent.NavigateToZoneDetails(zoneId))
        }
    }

    private suspend fun handleError(
        prefix: String,
        error: Throwable,
    ) {
        val message =
            when (error) {
                is ApiException -> error.error.message
                else -> "$prefix: ${error.message ?: "Unknown error"}"
            }
        _events.send(MapScreenEvent.ShowSnackbar(message))
    }
}
