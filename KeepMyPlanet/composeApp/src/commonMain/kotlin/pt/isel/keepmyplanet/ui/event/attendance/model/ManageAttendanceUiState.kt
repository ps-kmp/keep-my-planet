package pt.isel.keepmyplanet.ui.event.attendance.model

import pt.isel.keepmyplanet.domain.event.Event
import pt.isel.keepmyplanet.ui.base.UiState
import pt.isel.keepmyplanet.ui.user.profile.model.UserInfo

data class ManageAttendanceUiState(
    val event: Event? = null,
    val participants: List<UserInfo> = emptyList(),
    val attendees: List<UserInfo> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
) : UiState {
    val remainingParticipants: List<UserInfo>
        get() = participants - attendees.toSet()
}
