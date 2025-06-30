package pt.isel.keepmyplanet.ui.stats.states

import pt.isel.keepmyplanet.domain.event.EventListItem
import pt.isel.keepmyplanet.domain.user.UserStats
import pt.isel.keepmyplanet.ui.base.UiState

data class UserStatsUiState(
    val stats: UserStats? = null,
    val attendedEvents: List<EventListItem> = emptyList(),
    val isLoading: Boolean = true,
    val isAddingMore: Boolean = false,
    val offset: Int = 0,
    val limit: Int = 20,
    val hasMorePages: Boolean = true,
    val error: String? = null,
) : UiState
