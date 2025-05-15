package pt.isel.keepmyplanet.ui.screens.event

import pt.isel.keepmyplanet.data.model.EventInfo
import pt.isel.keepmyplanet.data.model.UserInfo
import pt.isel.keepmyplanet.domain.user.User
import pt.isel.keepmyplanet.dto.event.EventResponse

data class EventUiState(
    val user: UserInfo,
    val event: EventInfo,
    val participants: List<User> = emptyList(),
    val isLoading: Boolean = true,
    val isLoadingParticipants: Boolean = false,
    val isJoiningOrLeaving: Boolean = false,
    val isCancelling: Boolean = false,
    val isCompleting: Boolean = false,
)

data class EventListUiState(
    val events: List<EventInfo> = emptyList(),
    val isLoading: Boolean = false,
    val query: String = "",
    val error: String? = null,
)

data class EventDetailsUiState(
    val event: EventResponse? = null,
    val isLoading: Boolean = false,
    val isJoining: Boolean = false,
    val isLeaving: Boolean = false,
    val isEditing: Boolean = false,
    val error: String? = null,
)
