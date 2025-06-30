package pt.isel.keepmyplanet.ui.attendance.states

import pt.isel.keepmyplanet.ui.base.UiEvent

sealed interface ManageAttendanceEvent : UiEvent {
    data class ShowSnackbar(
        val message: String,
    ) : ManageAttendanceEvent
}
