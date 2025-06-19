package pt.isel.keepmyplanet.ui.user.stats.model

import pt.isel.keepmyplanet.domain.event.Event

data class UserStatsUiState(
    val attendedEvents: List<Event> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
)
