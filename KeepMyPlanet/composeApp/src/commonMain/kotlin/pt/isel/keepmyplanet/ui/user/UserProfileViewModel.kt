@file:Suppress("ktlint:standard:filename")

package pt.isel.keepmyplanet.ui.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pt.isel.keepmyplanet.data.api.UserApi
import pt.isel.keepmyplanet.data.mapper.toUserInfo
import pt.isel.keepmyplanet.domain.user.Email
import pt.isel.keepmyplanet.domain.user.Name
import pt.isel.keepmyplanet.domain.user.Password
import pt.isel.keepmyplanet.dto.user.ChangePasswordRequest
import pt.isel.keepmyplanet.dto.user.UpdateProfileRequest
import pt.isel.keepmyplanet.ui.user.model.UserInfo
import pt.isel.keepmyplanet.ui.user.model.UserProfileEvent
import pt.isel.keepmyplanet.ui.user.model.UserProfileUiState

class UserProfileViewModel(
    private val userService: UserApi,
    private val user: UserInfo,
) : ViewModel() {
    private val _uiState = MutableStateFlow(UserProfileUiState(userDetails = user))
    val uiState: StateFlow<UserProfileUiState> = _uiState.asStateFlow()

    private val _events = Channel<UserProfileEvent>(Channel.BUFFERED)
    val events: Flow<UserProfileEvent> = _events.receiveAsFlow()

    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            val result = userService.getUserDetails(user.id.value)
            result
                .onSuccess { userResponse ->
                    val updatedUser = userResponse.toUserInfo()
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            userDetails = updatedUser,
                            nameInput = if (it.isEditingProfile) it.nameInput else updatedUser.name.value,
                            emailInput = if (it.isEditingProfile) it.emailInput else updatedUser.email.value,
                        )
                    }
                }.onFailure { e ->
                    handleError("Failed to load profile", e, showSnackbar = true)
                }
        }
    }

    fun onNameChanged(newName: String) {
        _uiState.update { it.copy(nameInput = newName) }
    }

    fun onEmailChanged(newEmail: String) {
        _uiState.update { it.copy(emailInput = newEmail.trim()) }
    }

    fun onOldPasswordChanged(password: String) {
        _uiState.update { it.copy(oldPasswordInput = password) }
    }

    fun onNewPasswordChanged(password: String) {
        _uiState.update { it.copy(newPasswordInput = password) }
    }

    fun onConfirmPasswordChanged(password: String) {
        _uiState.update { it.copy(confirmPasswordInput = password) }
    }

    fun onEditProfileToggled() {
        val currentState = _uiState.value
        val isNowEditing = !currentState.isEditingProfile
        _uiState.update {
            it.copy(
                isEditingProfile = isNowEditing,
                nameInput = if (isNowEditing) it.userDetails?.name?.value ?: "" else it.nameInput,
                emailInput =
                    if (isNowEditing) it.userDetails?.email?.value ?: "" else it.emailInput,
            )
        }
    }

    fun onSaveProfileClicked() {
        val currentState = _uiState.value
        if (!currentState.isSaveProfileEnabled || !currentState.hasProfileChanges) return

        var nameToUpdate: String? = null
        var emailToUpdate: String? = null

        try {
            if (currentState.nameInput != currentState.userDetails?.name?.value) {
                Name(currentState.nameInput)
                nameToUpdate = currentState.nameInput
            }
            if (currentState.emailInput != currentState.userDetails?.email?.value) {
                Email(currentState.emailInput)
                emailToUpdate = currentState.emailInput
            }
        } catch (e: IllegalArgumentException) {
            viewModelScope.launch {
                _events.send(UserProfileEvent.ShowSnackbar(e.message ?: "Invalid input"))
            }
            return
        }

        _uiState.update { it.copy(isUpdatingProfile = true) }

        val request = UpdateProfileRequest(nameToUpdate, emailToUpdate, profilePictureId = null)

        viewModelScope.launch {
            val result = userService.updateUserProfile(user.id.value, request)

            result
                .onSuccess { updatedUserResponse ->
                    val updatedUser = updatedUserResponse.toUserInfo()
                    _uiState.update {
                        it.copy(
                            isUpdatingProfile = false,
                            isEditingProfile = false,
                            userDetails = updatedUser,
                            nameInput = updatedUser.name.value,
                            emailInput = updatedUser.email.value,
                        )
                    }
                    _events.send(UserProfileEvent.ShowSnackbar("Profile updated successfully"))
                }.onFailure { e ->
                    handleError("Failed to update profile", e, showSnackbar = true)
                }
        }
    }

    fun onPasswordChangeToggled() {
        _uiState.update {
            val show = !it.showPasswordChangeSection
            it.copy(
                showPasswordChangeSection = show,
                oldPasswordInput = if (!show) "" else it.oldPasswordInput,
                newPasswordInput = if (!show) "" else it.newPasswordInput,
                confirmPasswordInput = if (!show) "" else it.confirmPasswordInput,
            )
        }
    }

    fun onChangePasswordClicked() {
        val currentState = _uiState.value
        if (!currentState.isChangePasswordEnabled) return

        try {
            Password(currentState.oldPasswordInput)
        } catch (e: IllegalArgumentException) {
            viewModelScope.launch {
                _events.send(UserProfileEvent.ShowSnackbar("Old password: ${e.message}"))
            }
            return
        }

        try {
            Password(currentState.newPasswordInput)
        } catch (e: IllegalArgumentException) {
            viewModelScope.launch {
                _events.send(UserProfileEvent.ShowSnackbar("New password: ${e.message}"))
            }
            return
        }

        if (currentState.newPasswordInput != currentState.confirmPasswordInput) {
            viewModelScope.launch {
                _events.send(UserProfileEvent.ShowSnackbar("New passwords do not match"))
            }
            return
        }
        if (currentState.newPasswordInput == currentState.oldPasswordInput) {
            viewModelScope.launch {
                _events.send(UserProfileEvent.ShowSnackbar("New password cannot be the same as the old password"))
            }
            return
        }

        _uiState.update { it.copy(isChangingPassword = true) }

        val request =
            ChangePasswordRequest(
                oldPassword = currentState.oldPasswordInput,
                newPassword = currentState.newPasswordInput,
            )

        viewModelScope.launch {
            val result = userService.changePassword(user.id.value, request)

            result
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isChangingPassword = false,
                            showPasswordChangeSection = false,
                            oldPasswordInput = "",
                            newPasswordInput = "",
                            confirmPasswordInput = "",
                        )
                    }
                    _events.send(UserProfileEvent.ShowSnackbar("Password changed successfully"))
                }.onFailure { e ->
                    handleError("Failed to change password", e, showSnackbar = true)
                }
        }
    }

    fun onDeleteAccountClicked() {
        val currentState = _uiState.value
        if (!currentState.isDeleteAccountEnabled) return

        _uiState.update { it.copy(isDeletingAccount = true) }

        viewModelScope.launch {
            val result = userService.deleteUser(user.id.value)

            result
                .onSuccess {
                    _events.send(UserProfileEvent.ShowSnackbar("Account deleted successfully"))
                    _events.send(UserProfileEvent.NavigateToLogin)
                }.onFailure { e ->
                    handleError("Failed to delete account", e, showSnackbar = true)
                }
        }
    }

    private suspend fun handleError(
        prefix: String,
        exception: Throwable,
        showSnackbar: Boolean = false,
    ) {
        val errorMsg = "$prefix: ${exception.message ?: "Unknown error"}"
        _uiState.update {
            it.copy(
                isLoading = false,
                isUpdatingProfile = false,
                isChangingPassword = false,
                isDeletingAccount = false,
            )
        }
        if (showSnackbar) _events.send(UserProfileEvent.ShowSnackbar(errorMsg))
    }
}
