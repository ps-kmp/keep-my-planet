package pt.isel.keepmyplanet.ui.event.details.model

import pt.isel.keepmyplanet.domain.event.Event
import pt.isel.keepmyplanet.domain.event.EventStatus
import pt.isel.keepmyplanet.ui.base.UiState
import pt.isel.keepmyplanet.ui.user.profile.model.UserInfo

data class EventDetailsUiState(
    val event: Event? = null,
    val participants: List<UserInfo> = emptyList(),
    val isLoading: Boolean = false,
    val actionState: ActionState = ActionState.IDLE,
    val error: String? = null,
    val isCurrentUserOrganizer: Boolean = false,
    val isCurrentUserParticipant: Boolean = false,
) : UiState {
    enum class ActionState {
        IDLE,
        JOINING,
        LEAVING,
        CANCELLING,
        COMPLETING,
        DELETING,
    }

    fun canUserEdit(): Boolean =
        event != null && isCurrentUserOrganizer && event.status == EventStatus.PLANNED

    fun canUserJoin(): Boolean =
        event != null &&
            actionState == ActionState.IDLE &&
            event.status == EventStatus.PLANNED &&
            !event.isFull &&
            !isCurrentUserOrganizer &&
            !isCurrentUserParticipant

    fun canUserLeave(): Boolean =
        event != null &&
            actionState == ActionState.IDLE &&
            event.status == EventStatus.PLANNED &&
            isCurrentUserParticipant &&
            !isCurrentUserOrganizer

    fun canUserAccessChat(): Boolean =
        event != null && (isCurrentUserOrganizer || isCurrentUserParticipant)

    val isChatReadOnly: Boolean
        get() = event?.status in listOf(EventStatus.COMPLETED, EventStatus.CANCELLED)

    fun canOrganizerCancel(): Boolean =
        event != null &&
            actionState == ActionState.IDLE &&
            isCurrentUserOrganizer &&
            event.status in listOf(EventStatus.PLANNED, EventStatus.IN_PROGRESS)

    fun canOrganizerComplete(): Boolean =
        event != null &&
            actionState == ActionState.IDLE &&
            isCurrentUserOrganizer &&
            event.status == EventStatus.IN_PROGRESS

    fun canOrganizerDelete(): Boolean =
        event != null &&
            actionState == ActionState.IDLE &&
            isCurrentUserOrganizer &&
            event.status in listOf(EventStatus.PLANNED, EventStatus.CANCELLED)

    fun canManageAttendance(): Boolean =
        event != null && isCurrentUserOrganizer && event.status == EventStatus.IN_PROGRESS

    fun canUseQrFeature(): Boolean =
        event != null &&
            event.status == EventStatus.IN_PROGRESS &&
            (isCurrentUserOrganizer || isCurrentUserParticipant)
}
