package pt.isel.keepmyplanet.ui.event.details.model

import pt.isel.keepmyplanet.domain.event.Event
import pt.isel.keepmyplanet.domain.event.EventStatus
import pt.isel.keepmyplanet.domain.zone.Zone
import pt.isel.keepmyplanet.ui.user.model.UserInfo

data class EventDetailsUiState(
    val event: Event? = null,
    val organizer: UserInfo? = null,
    val participants: List<UserInfo> = emptyList(),
    val zone: Zone? = null,
    val isLoading: Boolean = false,
    val isJoining: Boolean = false,
    val isLeaving: Boolean = false,
    val isCancelling: Boolean = false,
    val isCompleting: Boolean = false,
    val isDeleting: Boolean = false,
    val error: String? = null,
    val isCurrentUserOrganizer: Boolean = false,
    val isCurrentUserParticipant: Boolean = false,
) {
    fun canUserEdit(): Boolean =
        event != null && isCurrentUserOrganizer && event.status == EventStatus.PLANNED

    fun canUserJoin(): Boolean =
        event != null &&
            !isJoining &&
            event.status == EventStatus.PLANNED &&
            !event.isFull &&
            !isCurrentUserOrganizer &&
            !isCurrentUserParticipant

    fun canUserLeave(): Boolean =
        event != null &&
            !isLeaving &&
            event.status == EventStatus.PLANNED &&
            isCurrentUserParticipant &&
            !isCurrentUserOrganizer

    fun canUserAccessChat(): Boolean =
        event != null && (isCurrentUserOrganizer || isCurrentUserParticipant)

    val isChatReadOnly: Boolean
        get() = event?.status in listOf(EventStatus.COMPLETED, EventStatus.CANCELLED)

    fun canOrganizerCancel(): Boolean =
        event != null &&
            !isCancelling &&
            isCurrentUserOrganizer &&
            event.status in listOf(EventStatus.PLANNED, EventStatus.IN_PROGRESS)

    fun canOrganizerComplete(): Boolean =
        event != null &&
            !isCompleting &&
            isCurrentUserOrganizer &&
            event.status == EventStatus.IN_PROGRESS

    fun canOrganizerDelete(): Boolean =
        event != null &&
            !isDeleting &&
            isCurrentUserOrganizer &&
            event.status in listOf(EventStatus.PLANNED, EventStatus.CANCELLED)

    fun canManageAttendance(): Boolean =
        event != null && isCurrentUserOrganizer && event.status == EventStatus.IN_PROGRESS

    fun canUseQrFeature(): Boolean =
        event != null &&
            event.status == EventStatus.IN_PROGRESS &&
            (isCurrentUserOrganizer || isCurrentUserParticipant)
}
