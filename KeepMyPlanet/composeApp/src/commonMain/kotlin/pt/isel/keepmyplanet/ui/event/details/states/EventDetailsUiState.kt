package pt.isel.keepmyplanet.ui.event.details.states

import pt.isel.keepmyplanet.domain.event.Event
import pt.isel.keepmyplanet.domain.event.EventStatus
import pt.isel.keepmyplanet.domain.user.UserInfo
import pt.isel.keepmyplanet.domain.user.UserRole
import pt.isel.keepmyplanet.ui.base.UiState

data class EventDetailsUiState(
    val event: Event? = null,
    val participants: List<UserInfo> = emptyList(),
    val isLoading: Boolean = false,
    val actionState: ActionState = ActionState.IDLE,
    val error: String? = null,
    val currentUser: UserInfo? = null,
    val showNotificationDialog: Boolean = false,
    val notificationTitle: String = "",
    val notificationMessage: String = "",
    val notificationError: String? = null,
) : UiState {
    enum class ActionState {
        IDLE,
        JOINING,
        LEAVING,
        CANCELLING,
        COMPLETING,
        DELETING,
        INITIATING_TRANSFER,
        RESPONDING_TO_TRANSFER,
        SENDING_NOTIFICATION,
    }

    val isGuest: Boolean
        get() = currentUser == null

    val isCurrentUserOrganizer: Boolean
        get() = event != null && currentUser != null && event.organizerId == currentUser.id

    private val isCurrentUserParticipant: Boolean
        get() =
            event != null && currentUser != null && event.participantsIds.contains(currentUser.id)

    val isCurrentUserPendingNominee: Boolean
        get() = event != null && currentUser != null && event.pendingOrganizerId == currentUser.id

    val isActionInProgress: Boolean
        get() = actionState != ActionState.IDLE

    val canUseQrFeature: Boolean
        get() =
            event != null &&
                event.status == EventStatus.IN_PROGRESS &&
                (isCurrentUserOrganizer || isCurrentUserParticipant)

    val canSeeStats: Boolean
        get() =
            event != null &&
                event.status in listOf(EventStatus.IN_PROGRESS, EventStatus.COMPLETED)

    val canUserJoin: Boolean
        get() =
            event != null &&
                event.status == EventStatus.PLANNED &&
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

    val canUserEdit: Boolean get() =
        event != null &&
            (isCurrentUserOrganizer || currentUser?.role == UserRole.ADMIN) &&
            event.status == EventStatus.PLANNED

    val canCompleteEvent: Boolean
        get() =
            event != null &&
                (isCurrentUserOrganizer || currentUser?.role == UserRole.ADMIN) &&
                event.status == EventStatus.IN_PROGRESS

    val canCancelEvent: Boolean
        get() =
            event != null &&
                (isCurrentUserOrganizer || currentUser?.role == UserRole.ADMIN) &&
                event.status in listOf(EventStatus.PLANNED, EventStatus.IN_PROGRESS)

    val canEventBeDeleted: Boolean
        get() =
            event != null &&
                (isCurrentUserOrganizer || currentUser?.role == UserRole.ADMIN) &&
                event.status in listOf(EventStatus.PLANNED, EventStatus.CANCELLED)

    val showCleanlinessConfirmation: Boolean
        get() = event?.status == EventStatus.COMPLETED && isCurrentUserOrganizer

    val canTransferOwnership: Boolean
        get() =
            event != null &&
                isCurrentUserOrganizer &&
                event.status in listOf(EventStatus.PLANNED, EventStatus.IN_PROGRESS) &&
                event.pendingOrganizerId == null

    val canOrganizerSendNotification: Boolean
        get() =
            isCurrentUserOrganizer &&
                event?.status in listOf(EventStatus.PLANNED, EventStatus.IN_PROGRESS)

    val isSendNotificationButtonEnabled: Boolean
        get() =
            notificationTitle.isNotBlank() &&
                notificationMessage.isNotBlank() &&
                !isActionInProgress
}
