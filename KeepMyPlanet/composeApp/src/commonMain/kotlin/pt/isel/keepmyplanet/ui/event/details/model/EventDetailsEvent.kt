package pt.isel.keepmyplanet.ui.event.details.model

import pt.isel.keepmyplanet.domain.common.Id

sealed interface EventDetailsEvent {
    data class ShowSnackbar(
        val message: String,
    ) : EventDetailsEvent

    data object EventDeleted : EventDetailsEvent

    data class NavigateToManageAttendance(
        val eventId: Id,
    ) : EventDetailsEvent

    data class NavigateToMyQrCode(
        val userId: Id,
    ) : EventDetailsEvent
}
