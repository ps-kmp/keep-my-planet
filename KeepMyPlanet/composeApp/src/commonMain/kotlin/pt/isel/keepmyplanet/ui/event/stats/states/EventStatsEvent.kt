package pt.isel.keepmyplanet.ui.event.stats.states

import pt.isel.keepmyplanet.ui.base.UiEvent

sealed interface EventStatsEvent : UiEvent {
    data class ShowSnackbar(
        val message: String,
    ) : EventStatsEvent
}
