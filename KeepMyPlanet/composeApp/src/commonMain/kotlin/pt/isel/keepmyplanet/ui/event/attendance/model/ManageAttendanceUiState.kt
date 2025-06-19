package pt.isel.keepmyplanet.ui.event.attendance.model

import pt.isel.keepmyplanet.domain.event.Event
import pt.isel.keepmyplanet.ui.user.model.UserInfo

data class ManageAttendanceUiState(
    val event: Event? = null,
    val participants: List<UserInfo> = emptyList(), // Users in the event
    val attendees: List<UserInfo> = emptyList(), // Users who checked in
    val isLoading: Boolean = true,
    val error: String? = null,
    val checkInStatusMessage: String? = null, // Para feedback do scan
)
