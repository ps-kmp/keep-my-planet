package pt.isel.keepmyplanet.ui.admin.states

import pt.isel.keepmyplanet.ui.base.UiEvent

sealed interface UserListEvent : UiEvent {
    data class ShowSnackbar(
        val message: String,
    ) : UserListEvent

    data object ShowRoleChangeDialog : UserListEvent

    data object HideRoleChangeDialog : UserListEvent

    data object ShowDeleteDialog : UserListEvent

    data object HideDeleteDialog : UserListEvent
}
