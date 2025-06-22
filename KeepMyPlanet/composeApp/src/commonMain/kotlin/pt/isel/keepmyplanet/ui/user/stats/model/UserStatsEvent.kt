package pt.isel.keepmyplanet.ui.user.stats.model

sealed interface UserStatsEvent {
    data class ShowSnackbar(
        val message: String,
    ) : UserStatsEvent
}
