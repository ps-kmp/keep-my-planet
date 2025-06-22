package pt.isel.keepmyplanet.ui.event.attendance.model

sealed interface ManageAttendanceEvent {
    data class ShowSnackbar(
        val message: String,
    ) : ManageAttendanceEvent
}
