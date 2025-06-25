package pt.isel.keepmyplanet.ui.event.history.states

import pt.isel.keepmyplanet.dto.event.EventStateChangeResponse
import pt.isel.keepmyplanet.ui.viewmodel.UiState

data class EventStatusHistoryUiState(
    val history: List<EventStateChangeResponse> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
) : UiState
