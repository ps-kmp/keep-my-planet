package pt.isel.keepmyplanet.ui.event.model

import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.domain.event.Event
import pt.isel.keepmyplanet.domain.event.EventStatus

data class EventListUiState(
    val events: List<EventListItem> = emptyList(),
    val isLoading: Boolean = false,
    val query: String = "",
    val error: String? = null,
    val limit: Int = 20,
    val offset: Int = 0,
) {
    val canLoadPrevious: Boolean get() = !isLoading && offset > 0
    val canLoadNext: Boolean get() = !isLoading && events.size == limit
}

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

    fun canUserEdit(userId: Id): Boolean = event != null && event.organizerId == userId

    fun canUserChat(userId: Id): Boolean = event != null && event.isUserParticipantOrOrganizer(userId)
}

data class EventFormUiState(
    val title: String = "",
    val description: String = "",
    val startDate: String = "",
    val maxParticipants: String = "",
    val zoneId: String = "",
    val isSubmitting: Boolean = false,
) {
    val canSubmit: Boolean
        get() =
            title.isNotBlank() &&
                description.isNotBlank() &&
                startDate.isNotBlank() &&
                zoneId.toUIntOrNull() != null &&
                !isSubmitting
}
