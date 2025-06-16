package pt.isel.keepmyplanet.ui.event.model

data class EventListUiState(
    val events: List<EventListItem> = emptyList(),
    val isLoading: Boolean = false,
    val isAddingMore: Boolean = false,
    val query: String = "",
    val filter: EventFilterType = EventFilterType.ALL,
    val limit: Int = 10,
    val offset: Int = 0,
    val hasMorePages: Boolean = true,
    val error: String? = null,
)
