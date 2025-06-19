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
import pt.isel.keepmyplanet.data.http.ApiException
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
    private val onProfileUpdated: (UserInfo) -> Unit,
) : ViewModel() {
    private val _uiState = MutableStateFlow(UserProfileUiState(userDetails = user))
    val uiState: StateFlow<UserProfileUiState> = _uiState.asStateFlow()

    private val _events = Channel<UserProfileEvent>(Channel.BUFFERED)
    val events: Flow<UserProfileEvent> = _events.receiveAsFlow()

    init {
        loadUserProfile()
    }

    fun onNameChanged(newName: String) {
        _uiState.update { it.copy(nameInput = newName, nameInputError = null) }
    }

    fun onEmailChanged(newEmail: String) {
        _uiState.update { it.copy(emailInput = newEmail.trim(), emailInputError = null) }
    }

    fun onOldPasswordChanged(password: String) {
        _uiState.update { it.copy(oldPasswordInput = password) }
    }

    fun onNewPasswordChanged(password: String) {
        _uiState.update { it.copy(newPasswordInput = password, newPasswordInputError = null) }
    }

    fun onConfirmPasswordChanged(password: String) {
        _uiState.update {
            it.copy(confirmPasswordInput = password, confirmPasswordInputError = null)
        }
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
                nameInputError = null,
                emailInputError = null,
            )
        }
    }

    fun onPasswordChangeToggled() {
        _uiState.update {
            val show = !it.showPasswordChangeSection
            it.copy(
                showPasswordChangeSection = show,
                oldPasswordInput = "",
                newPasswordInput = "",
                confirmPasswordInput = "",
                newPasswordInputError = null,
                confirmPasswordInputError = null,
            )
        }
    }

    fun onSaveProfileClicked() {
        if (!validateProfileForm()) return

        val currentState = _uiState.value
        if (!currentState.isSaveProfileEnabled) return

        val nameToUpdate =
            if (currentState.nameInput != currentState.userDetails?.name?.value) {
                currentState.nameInput
            } else {
                null
            }
        val emailToUpdate =
            if (currentState.emailInput != currentState.userDetails?.email?.value) {
                currentState.emailInput
            } else {
                null
            }
        val request = UpdateProfileRequest(nameToUpdate, emailToUpdate, profilePictureId = null)

        viewModelScope.launch {
            _uiState.update { it.copy(isUpdatingProfile = true) }
            val result = userService.updateUserProfile(user.id.value, request)
            result
                .onSuccess { updatedUserResponse ->
                    val updatedUser = updatedUserResponse.toUserInfo()
                    onProfileUpdated(updatedUser)
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
                    handleError("Failed to update profile", e)
                }
            _uiState.update { it.copy(isUpdatingProfile = false) }
        }
    }

    fun onChangePasswordClicked() {
        if (!validatePasswordChangeForm()) return

        val currentState = _uiState.value
        if (!currentState.isChangePasswordEnabled) return

        viewModelScope.launch {
            _uiState.update { it.copy(isChangingPassword = true) }
            val request =
                ChangePasswordRequest(currentState.oldPasswordInput, currentState.newPasswordInput)
            val result = userService.changePassword(user.id.value, request)

            result
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            showPasswordChangeSection = false,
                        )
                    } // Hide on success
                    _events.send(UserProfileEvent.ShowSnackbar("Password changed successfully"))
                }.onFailure { e ->
                    handleError("Failed to change password", e)
                }
            _uiState.update { it.copy(isChangingPassword = false) }
        }
    }

    fun loadUserProfile() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            val result = userService.getUserDetails(user.id.value)
            result
                .onSuccess { userResponse ->
                    val updatedUser = userResponse.toUserInfo()
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            userDetails = updatedUser,
                            nameInput =
                                if (it.isEditingProfile) it.nameInput else updatedUser.name.value,
                            emailInput =
                                if (it.isEditingProfile) it.emailInput else updatedUser.email.value,
                        )
                    }
                }.onFailure { e ->
                    val errorMessage =
                        when (e) {
                            is ApiException -> e.error.message
                            else -> "Failed to load profile: ${e.message ?: "Unknown error"}"
                        }
                    _uiState.update { it.copy(isLoading = false, error = errorMessage) }
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
                    _events.send(UserProfileEvent.AccountDeleted)
                }.onFailure { e ->
                    handleError("Failed to delete account", e)
                }
        }
    }

    private fun validateProfileForm(): Boolean {
        val state = _uiState.value
        val nameError =
            try {
                Name(state.nameInput)
                null
            } catch (e: IllegalArgumentException) {
                e.message
            }
        val emailError =
            try {
                Email(state.emailInput)
                null
            } catch (e: IllegalArgumentException) {
                e.message
            }

        _uiState.update { it.copy(nameInputError = nameError, emailInputError = emailError) }
        return !_uiState.value.hasProfileErrors
    }

    private fun validatePasswordChangeForm(): Boolean {
        val state = _uiState.value
        val newPasswordError =
            try {
                Password(state.newPasswordInput)
                null
            } catch (e: IllegalArgumentException) {
                e.message
            }
        var confirmPasswordError: String? = null

        if (newPasswordError == null) {
            if (state.newPasswordInput != state.confirmPasswordInput) {
                confirmPasswordError = "Passwords do not match"
            }
            if (state.newPasswordInput == state.oldPasswordInput) {
                viewModelScope.launch {
                    _events.send(
                        UserProfileEvent.ShowSnackbar(
                            "New password cannot be the same as the old one.",
                        ),
                    )
                }
                return false
            }
        }

        _uiState.update {
            it.copy(
                newPasswordInputError = newPasswordError,
                confirmPasswordInputError = confirmPasswordError,
            )
        }
        return !_uiState.value.hasPasswordErrors
    }

    private suspend fun handleError(
        prefix: String,
        exception: Throwable,
    ) {
        val errorMsg =
            when (exception) {
                is ApiException -> exception.error.message
                else -> "$prefix: ${exception.message ?: "Unknown error"}"
            }
        _uiState.update {
            it.copy(
                isLoading = false,
                isUpdatingProfile = false,
                isChangingPassword = false,
                isDeletingAccount = false,
            )
        }
        _events.send(UserProfileEvent.ShowSnackbar(errorMsg))
    }
}
