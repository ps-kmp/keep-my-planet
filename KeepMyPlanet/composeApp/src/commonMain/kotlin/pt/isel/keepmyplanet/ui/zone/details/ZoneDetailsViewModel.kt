package pt.isel.keepmyplanet.ui.zone.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pt.isel.keepmyplanet.data.api.ZoneApi
import pt.isel.keepmyplanet.data.http.ApiException
import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.mapper.zone.toZone
import pt.isel.keepmyplanet.ui.zone.details.model.ZoneDetailsUiState

class ZoneDetailsViewModel(
    private val zoneApi: ZoneApi,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ZoneDetailsUiState())
    val uiState = _uiState.asStateFlow()

    fun loadZoneDetails(zoneId: Id) {
        _uiState.update { it.copy(isLoading = true, zone = null, error = null) }
        viewModelScope.launch {
            zoneApi
                .getZoneDetails(zoneId.value)
                .onSuccess { response ->
                    _uiState.update { it.copy(isLoading = false, zone = response.toZone()) }
                }.onFailure { error ->
                    val errorMessage =
                        when (error) {
                            is ApiException -> error.error.message
                            else ->
                                "Failed to load zone details: " +
                                    (error.message ?: "Unknown error")
                        }
                    _uiState.update { it.copy(isLoading = false, error = errorMessage) }
                }
        }
    }
}
