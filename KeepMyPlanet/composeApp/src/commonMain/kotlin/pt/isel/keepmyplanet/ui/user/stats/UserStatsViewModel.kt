package pt.isel.keepmyplanet.ui.user.stats

import androidx.lifecycle.ViewModel
import pt.isel.keepmyplanet.data.api.EventApi
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pt.isel.keepmyplanet.mapper.event.toEvent
import pt.isel.keepmyplanet.ui.user.stats.model.UserStatsUiState

class UserStatsViewModel(
    private val eventApi: EventApi
) : ViewModel() {
    private val _uiState = MutableStateFlow(UserStatsUiState())
    val uiState: StateFlow<UserStatsUiState> = _uiState.asStateFlow()

    init {
        loadAttendedEvents()
    }

    fun loadAttendedEvents() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            //Add pagination
            eventApi.getAttendedEvents(limit = 20, offset = 0)
                .onSuccess { response ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            attendedEvents = response.map { e -> e.toEvent() }
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Failed to load stats: ${error.message}"
                        )
                    }
                }
        }
    }
}
