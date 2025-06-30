package pt.isel.keepmyplanet.ui.event.details.states

import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.ui.base.UiEvent

sealed interface EventDetailsEvent : UiEvent {
    data class ShowSnackbar(
        val message: String,
    ) : EventDetailsEvent

    data object EventDeleted : EventDetailsEvent

    data class NavigateToManageAttendance(
        val eventId: Id,
    ) : EventDetailsEvent

    data class NavigateToMyQrCode(
        val userId: Id,
        val organizerName: String,
    ) : EventDetailsEvent
}
