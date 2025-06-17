package pt.isel.keepmyplanet.ui.event.details.model

import pt.isel.keepmyplanet.domain.common.Id
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
) {
    fun isUserOrganizer(userId: Id): Boolean = event?.organizerId == userId

    fun isUserParticipant(userId: Id): Boolean = event?.participantsIds?.contains(userId) == true

    fun canUserEdit(userId: Id): Boolean = event != null && isUserOrganizer(userId) && event.status == EventStatus.PLANNED

    fun canUserJoin(userId: Id): Boolean =
        event != null &&
            !isJoining &&
            event.status == EventStatus.PLANNED &&
            !event.isFull &&
            !isUserOrganizer(userId) &&
            !isUserParticipant(userId)

    fun canUserLeave(userId: Id): Boolean =
        event != null &&
            !isLeaving &&
            event.status == EventStatus.PLANNED &&
            isUserParticipant(userId) &&
            !isUserOrganizer(userId)

    fun canUserAccessChat(userId: Id): Boolean = event != null && (isUserOrganizer(userId) || isUserParticipant(userId))

    val isChatReadOnly: Boolean
        get() = event?.status in listOf(EventStatus.COMPLETED, EventStatus.CANCELLED)

    fun canOrganizerCancel(userId: Id): Boolean =
        event != null &&
            !isCancelling &&
            isUserOrganizer(userId) &&
            event.status in listOf(EventStatus.PLANNED, EventStatus.IN_PROGRESS)

    fun canOrganizerComplete(userId: Id): Boolean =
        event != null &&
            !isCompleting &&
            isUserOrganizer(userId) &&
            event.status == EventStatus.IN_PROGRESS

    fun canOrganizerDelete(userId: Id): Boolean =
        event != null &&
            !isDeleting &&
            isUserOrganizer(userId) &&
            event.status in listOf(EventStatus.PLANNED, EventStatus.CANCELLED)
}
