package pt.isel.keepmyplanet.ui.event.details.model

import pt.isel.keepmyplanet.dto.event.EventStateChangeResponse
import pt.isel.keepmyplanet.ui.base.UiState

data class EventStatusHistoryUiState(
    val history: List<EventStateChangeResponse> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
) : UiState
