package pt.isel.keepmyplanet.ui.admin.states

import pt.isel.keepmyplanet.domain.user.UserInfo
import pt.isel.keepmyplanet.ui.base.UiState

data class UserListUiState(
    val users: List<UserInfo> = emptyList(),
    val currentUser: UserInfo? = null,
    val isLoading: Boolean = true,
    val isUpdatingRole: Boolean = false,
    val userToUpdate: UserInfo? = null,
    val isDeletingUser: Boolean = false,
    val userToDelete: UserInfo? = null,
    val error: String? = null,
) : UiState
