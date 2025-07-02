package pt.isel.keepmyplanet.ui.event.details.states

import pt.isel.keepmyplanet.domain.event.Event
import pt.isel.keepmyplanet.domain.event.EventStatus
import pt.isel.keepmyplanet.domain.user.UserInfo
import pt.isel.keepmyplanet.ui.base.UiState

data class EventDetailsUiState(
    val event: Event? = null,
    val participants: List<UserInfo> = emptyList(),
    val isLoading: Boolean = false,
    val actionState: ActionState = ActionState.IDLE,
    val error: String? = null,
    val isCurrentUserOrganizer: Boolean = false,
    val isCurrentUserParticipant: Boolean = false,
    val isCurrentUserPendingNominee: Boolean = false
) : UiState {
    enum class ActionState {
        IDLE,
        JOINING,
        LEAVING,
        CANCELLING,
        COMPLETING,
        DELETING,
        INITIATING_TRANSFER,
        RESPONDING_TO_TRANSFER
    }

    val isActionInProgress: Boolean
        get() = actionState != ActionState.IDLE

    val canUseQrFeature: Boolean
        get() =
            event != null &&
                event.status == EventStatus.IN_PROGRESS &&
                (isCurrentUserOrganizer || isCurrentUserParticipant)

    val canUserJoin: Boolean
        get() =
            event != null &&
                event.status in listOf(EventStatus.PLANNED, EventStatus.IN_PROGRESS) &&
                !event.isFull &&
                !isCurrentUserOrganizer &&
                !isCurrentUserParticipant

    val canUserLeave: Boolean
        get() =
            event != null &&
                event.status == EventStatus.PLANNED &&
                isCurrentUserParticipant &&
                !isCurrentUserOrganizer

    val canUserAccessChat: Boolean
        get() = event != null && (isCurrentUserOrganizer || isCurrentUserParticipant)

    val isChatReadOnly: Boolean
        get() = event?.status in listOf(EventStatus.COMPLETED, EventStatus.CANCELLED)

    val canUserEdit: Boolean
        get() = event != null && isCurrentUserOrganizer && event.status == EventStatus.PLANNED

    val canOrganizerComplete: Boolean
        get() =
            event != null &&
                isCurrentUserOrganizer &&
                event.status == EventStatus.IN_PROGRESS

    val canOrganizerCancel: Boolean
        get() =
            event != null &&
                isCurrentUserOrganizer &&
                event.status in listOf(EventStatus.PLANNED, EventStatus.IN_PROGRESS)

    val canOrganizerDelete: Boolean
        get() =
            event != null &&
                isCurrentUserOrganizer &&
                event.status in listOf(EventStatus.PLANNED, EventStatus.CANCELLED)

    val showCleanlinessConfirmation: Boolean
        get() = event?.status == EventStatus.COMPLETED && isCurrentUserOrganizer

    val canTransferOwnership: Boolean
        get() = event != null &&
            isCurrentUserOrganizer &&
            event.status in listOf(EventStatus.PLANNED, EventStatus.IN_PROGRESS) &&
            event.pendingOrganizerId == null

}
