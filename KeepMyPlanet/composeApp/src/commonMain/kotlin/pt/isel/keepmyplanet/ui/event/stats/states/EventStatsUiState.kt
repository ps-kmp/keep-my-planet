package pt.isel.keepmyplanet.ui.event.stats.states

import pt.isel.keepmyplanet.domain.event.EventStats
import pt.isel.keepmyplanet.ui.base.UiState

data class EventStatsUiState(
    val stats: EventStats? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
) : UiState
