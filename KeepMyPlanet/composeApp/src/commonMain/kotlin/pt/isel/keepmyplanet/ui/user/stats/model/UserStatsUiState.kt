package pt.isel.keepmyplanet.ui.user.stats.model

import pt.isel.keepmyplanet.ui.event.list.model.EventListItem

data class UserStatsUiState(
    val attendedEvents: List<EventListItem> = emptyList(),
    val isLoading: Boolean = true,
    val isAddingMore: Boolean = false,
    val offset: Int = 0,
    val limit: Int = 20,
    val hasMorePages: Boolean = true,
    val error: String? = null,
)
