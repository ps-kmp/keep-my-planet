package pt.isel.keepmyplanet.ui.attendance.states

import pt.isel.keepmyplanet.domain.event.Event
import pt.isel.keepmyplanet.domain.user.UserInfo
import pt.isel.keepmyplanet.ui.base.UiState

enum class ManageAttendanceTab {
    SCANNER,
    ATTENDEES,
    REMAINING,
}

data class ManageAttendanceUiState(
    val event: Event? = null,
    val participants: List<UserInfo> = emptyList(),
    val attendees: List<UserInfo> = emptyList(),
    val isLoading: Boolean = true,
    val isCheckingIn: Boolean = false,
    val error: String? = null,
    val selectedTab: ManageAttendanceTab = ManageAttendanceTab.SCANNER,
) : UiState {
    val remainingParticipants: List<UserInfo>
        get() = participants - attendees.toSet()
}
