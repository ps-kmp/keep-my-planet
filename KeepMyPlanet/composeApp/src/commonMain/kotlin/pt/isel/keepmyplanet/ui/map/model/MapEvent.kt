package pt.isel.keepmyplanet.ui.map.model

sealed interface MapEvent {
    data class ShowSnackbar(
        val message: String,
    ) : MapEvent
}
