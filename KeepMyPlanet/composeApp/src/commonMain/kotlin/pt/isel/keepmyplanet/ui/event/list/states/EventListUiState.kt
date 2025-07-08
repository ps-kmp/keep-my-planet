package pt.isel.keepmyplanet.ui.event.list.states

import pt.isel.keepmyplanet.DEFAULT_PAGE_SIZE
import pt.isel.keepmyplanet.domain.event.EventFilterType
import pt.isel.keepmyplanet.domain.event.EventListItem
import pt.isel.keepmyplanet.ui.base.UiState

data class EventListUiState(
    val events: List<EventListItem> = emptyList(),
    val isLoading: Boolean = false,
    val isAddingMore: Boolean = false,
    val query: String = "",
    val filter: EventFilterType = EventFilterType.ALL,
    val limit: Int = DEFAULT_PAGE_SIZE,
    val offset: Int = 0,
    val hasMorePages: Boolean = true,
    val error: String? = null,
    val isGuest: Boolean = false,
) : UiState
