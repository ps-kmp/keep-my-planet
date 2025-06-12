package pt.isel.keepmyplanet.ui.event.model

import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.domain.event.Event
import pt.isel.keepmyplanet.domain.event.EventStatus

data class EventDetailsUiState(
    val event: Event? = null,
    val isLoading: Boolean = false,
    val isJoining: Boolean = false,
    val isLeaving: Boolean = false,
    val isEditing: Boolean = false,
    val error: String? = null,
) {
    fun canUserJoin(currentUserId: Id): Boolean =
        event != null &&
            !isJoining &&
            event.status == EventStatus.PLANNED &&
            !event.isUserParticipantOrOrganizer(currentUserId) &&
            !event.isFull

    fun canUserLeave(currentUserId: Id): Boolean =
        event != null &&
            !isLeaving &&
            !isJoining &&
            event.participantsIds.contains(currentUserId) &&
            event.organizerId != currentUserId

    fun canUserEdit(userId: Id): Boolean = event != null && event.organizerId == userId

    fun canUserChat(userId: Id): Boolean = event != null && event.isUserParticipantOrOrganizer(userId)
}
