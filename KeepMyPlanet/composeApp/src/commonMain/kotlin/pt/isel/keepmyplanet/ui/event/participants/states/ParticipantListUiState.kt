package pt.isel.keepmyplanet.ui.event.participants.states

import pt.isel.keepmyplanet.domain.event.Event
import pt.isel.keepmyplanet.domain.user.UserInfo
import pt.isel.keepmyplanet.ui.base.UiState

data class ParticipantListUiState(
    val event: Event? = null,
    val participants: List<UserInfo> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
) : UiState
