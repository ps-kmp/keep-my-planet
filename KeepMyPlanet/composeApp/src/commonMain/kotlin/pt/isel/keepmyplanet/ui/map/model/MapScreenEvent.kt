package pt.isel.keepmyplanet.ui.map.model

import pt.isel.keepmyplanet.domain.common.Id

sealed class MapScreenEvent {
    data class ShowSnackbar(
        val message: String,
    ) : MapScreenEvent()

    data class NavigateToZoneDetails(
        val zoneId: Id,
    ) : MapScreenEvent()
}
