package pt.isel.keepmyplanet.ui.attendance.states

import pt.isel.keepmyplanet.ui.viewmodel.UiEvent

sealed interface ManageAttendanceEvent : UiEvent {
    data class ShowSnackbar(
        val message: String,
    ) : ManageAttendanceEvent
}
