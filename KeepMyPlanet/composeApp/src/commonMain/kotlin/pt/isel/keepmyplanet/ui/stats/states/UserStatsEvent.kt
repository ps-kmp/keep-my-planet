package pt.isel.keepmyplanet.ui.stats.states

import pt.isel.keepmyplanet.ui.base.UiEvent

sealed interface UserStatsEvent : UiEvent {
    data class ShowSnackbar(
        val message: String,
    ) : UserStatsEvent
}
