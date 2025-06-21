package pt.isel.keepmyplanet.ui.event.attendance.model

import pt.isel.keepmyplanet.domain.event.Event
import pt.isel.keepmyplanet.ui.user.profile.model.UserInfo

data class ManageAttendanceUiState(
    val event: Event? = null,
    // Users in the event
    val participants: List<UserInfo> = emptyList(),
    // Users who checked in
    val attendees: List<UserInfo> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    // Para feedback do scan
    val checkInStatusMessage: String? = null,
)
