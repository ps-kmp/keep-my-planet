package pt.isel.keepmyplanet.ui.screens.event

import pt.isel.keepmyplanet.data.model.EventInfo
import pt.isel.keepmyplanet.dto.event.EventResponse

data class EventListUiState(
    val events: List<EventInfo> = emptyList(),
    val isLoading: Boolean = false,
    val query: String = "",
    val error: String? = null,
    val limit: Int = 20,
    val offset: Int = 0,
) {
    val canLoadPrevious: Boolean get() = !isLoading && offset > 0
    val canLoadNext: Boolean get() = !isLoading && events.size == limit
}

data class EventDetailsUiState(
    val event: EventResponse? = null,
    val isLoading: Boolean = false,
    val isJoining: Boolean = false,
    val isLeaving: Boolean = false,
    val isEditing: Boolean = false,
    val error: String? = null,
)
