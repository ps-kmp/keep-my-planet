package pt.isel.keepmyplanet.ui.screens.event

import pt.isel.keepmyplanet.data.model.EventInfo
import pt.isel.keepmyplanet.data.model.UserInfo
import pt.isel.keepmyplanet.domain.user.User

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
