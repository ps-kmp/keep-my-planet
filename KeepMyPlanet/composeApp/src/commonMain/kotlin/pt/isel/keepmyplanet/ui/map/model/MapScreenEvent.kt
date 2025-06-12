package pt.isel.keepmyplanet.ui.map.model

sealed class MapScreenEvent {
    data class ShowSnackbar(
        val message: String,
    ) : MapScreenEvent()

    data class NavigateToZoneDetails(
        val zoneId: UInt,
    ) : MapScreenEvent()
}
