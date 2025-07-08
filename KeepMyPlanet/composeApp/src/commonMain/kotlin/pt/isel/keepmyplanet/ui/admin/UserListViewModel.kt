package pt.isel.keepmyplanet.ui.admin

import pt.isel.keepmyplanet.data.repository.DefaultUserRepository
import pt.isel.keepmyplanet.domain.user.UserInfo
import pt.isel.keepmyplanet.domain.user.UserRole
import pt.isel.keepmyplanet.session.SessionManager
import pt.isel.keepmyplanet.ui.admin.states.UserListEvent
import pt.isel.keepmyplanet.ui.admin.states.UserListUiState
import pt.isel.keepmyplanet.ui.base.BaseViewModel

class UserListViewModel(
    private val userRepository: DefaultUserRepository,
    private val sessionManager: SessionManager,
) : BaseViewModel<UserListUiState>(UserListUiState()) {
    init {
        loadUsers()
    }

    fun loadUsers() {
        launchWithResult(
            onStart = { copy(isLoading = true, error = null) },
            onFinally = { copy(isLoading = false) },
            block = { userRepository.getAllUsers() },
            onSuccess = { users ->
                val currentUser = sessionManager.userSession.value?.userInfo
                setState {
                    copy(
                        users = users.filter { it.id != currentUser?.id },
                        currentUser = currentUser,
                    )
                }
            },
            onError = { throwable ->
                setState { copy(error = getErrorMessage("Failed to load users", throwable)) }
            },
        )
    }

    fun startRoleChange(user: UserInfo) {
        setState { copy(userToUpdate = user) }
        sendEvent(UserListEvent.ShowRoleChangeDialog)
    }

    fun cancelRoleChange() {
        setState { copy(userToUpdate = null) }
        sendEvent(UserListEvent.HideRoleChangeDialog)
    }

    fun confirmRoleChange() {
        val userToUpdate = currentState.userToUpdate ?: return
        val newRole = if (userToUpdate.role == UserRole.USER) UserRole.ADMIN else UserRole.USER

        launchWithResult(
            onStart = { copy(isUpdatingRole = true) },
            onFinally = {
                copy(isUpdatingRole = false, userToUpdate = null).also {
                    sendEvent(UserListEvent.HideRoleChangeDialog)
                }
            },
            block = { userRepository.updateUserRole(userToUpdate.id, newRole) },
            onSuccess = { updatedUser ->
                setState {
                    copy(users = users.map { if (it.id == updatedUser.id) updatedUser else it })
                }
                sendEvent(UserListEvent.ShowSnackbar("User role updated successfully."))
            },
            onError = { throwable ->
                handleErrorWithMessage(getErrorMessage("Failed to update user role", throwable))
            },
        )
    }

    fun startDelete(user: UserInfo) {
        setState { copy(userToDelete = user) }
        sendEvent(UserListEvent.ShowDeleteDialog)
    }

    fun cancelDelete() {
        setState { copy(userToDelete = null) }
        sendEvent(UserListEvent.HideDeleteDialog)
    }

    fun confirmDelete() {
        val userToDelete = currentState.userToDelete ?: return

        launchWithResult(
            onStart = { copy(isDeletingUser = true) },
            onFinally = {
                copy(isDeletingUser = false, userToDelete = null).also {
                    sendEvent(UserListEvent.HideDeleteDialog)
                }
            },
            block = { userRepository.deleteUser(userToDelete.id) },
            onSuccess = {
                setState {
                    copy(users = users.filter { it.id != userToDelete.id })
                }
                sendEvent(
                    UserListEvent.ShowSnackbar(
                        "User ${userToDelete.name.value} deleted successfully.",
                    ),
                )
            },
            onError = { throwable ->
                handleErrorWithMessage(getErrorMessage("Failed to delete user", throwable))
            },
        )
    }

    override fun handleErrorWithMessage(message: String) {
        sendEvent(UserListEvent.ShowSnackbar(message))
    }
}
