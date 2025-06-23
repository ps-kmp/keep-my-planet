package pt.isel.keepmyplanet.ui.user.stats.model

import pt.isel.keepmyplanet.ui.base.UiEvent

sealed interface UserStatsEvent : UiEvent {
    data class ShowSnackbar(
        val message: String,
    ) : UserStatsEvent
}
