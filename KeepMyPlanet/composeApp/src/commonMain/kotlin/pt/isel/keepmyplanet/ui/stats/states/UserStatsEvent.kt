package pt.isel.keepmyplanet.ui.stats.states

import pt.isel.keepmyplanet.ui.viewmodel.UiEvent

sealed interface UserStatsEvent : UiEvent {
    data class ShowSnackbar(
        val message: String,
    ) : UserStatsEvent
}
