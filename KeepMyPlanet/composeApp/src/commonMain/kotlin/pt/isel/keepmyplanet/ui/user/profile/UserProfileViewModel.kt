package pt.isel.keepmyplanet.ui.user.profile

import pt.isel.keepmyplanet.data.api.UserApi
import pt.isel.keepmyplanet.data.mapper.toUserInfo
import pt.isel.keepmyplanet.domain.user.Email
import pt.isel.keepmyplanet.domain.user.Name
import pt.isel.keepmyplanet.domain.user.Password
import pt.isel.keepmyplanet.dto.user.ChangePasswordRequest
import pt.isel.keepmyplanet.dto.user.UpdateProfileRequest
import pt.isel.keepmyplanet.ui.base.BaseViewModel
import pt.isel.keepmyplanet.ui.user.profile.model.UserInfo
import pt.isel.keepmyplanet.ui.user.profile.model.UserProfileEvent
import pt.isel.keepmyplanet.ui.user.profile.model.UserProfileUiState

class UserProfileViewModel(
    private val userApi: UserApi,
    private val user: UserInfo,
) : BaseViewModel<UserProfileUiState>(UserProfileUiState(user)) {
    init {
        loadUserProfile()
    }

    override fun handleErrorWithMessage(message: String) {
        setState { copy(isLoading = false, actionState = UserProfileUiState.ActionState.IDLE) }
        sendEvent(UserProfileEvent.ShowSnackbar(message))
    }

    fun onNameChanged(newName: String) =
        setState { copy(nameInput = newName, nameInputError = null) }

    fun onEmailChanged(newEmail: String) =
        setState {
            copy(emailInput = newEmail.trim(), emailInputError = null)
        }

    fun onOldPasswordChanged(password: String) =
        setState {
            copy(oldPasswordInput = password)
        }

    fun onNewPasswordChanged(password: String) =
        setState {
            copy(newPasswordInput = password, newPasswordInputError = null)
        }

    fun onConfirmPasswordChanged(password: String) =
        setState {
            copy(confirmPasswordInput = password, confirmPasswordInputError = null)
        }

    fun onEditProfileToggled() {
        val isNowEditing = !currentState.isEditingProfile
        setState {
            copy(
                isEditingProfile = isNowEditing,
                nameInput = if (isNowEditing) userDetails?.name?.value ?: "" else nameInput,
                emailInput = if (isNowEditing) userDetails?.email?.value ?: "" else emailInput,
                nameInputError = null,
                emailInputError = null,
            )
        }
    }

    fun onPasswordChangeToggled() {
        setState {
            copy(
                showPasswordChangeSection = !showPasswordChangeSection,
                oldPasswordInput = "",
                newPasswordInput = "",
                confirmPasswordInput = "",
                newPasswordInputError = null,
                confirmPasswordInputError = null,
            )
        }
    }

    fun loadUserProfile() {
        launchWithResult(
            onStart = { copy(isLoading = true, error = null) },
            onFinally = { copy(isLoading = false) },
            block = { userApi.getUserDetails(user.id.value) },
            onSuccess = { userResponse ->
                val updatedUser = userResponse.toUserInfo()
                if (updatedUser != currentState.userDetails) {
                    sendEvent(UserProfileEvent.ProfileUpdated(updatedUser))
                }
                setState {
                    copy(
                        userDetails = updatedUser,
                        nameInput = if (isEditingProfile) nameInput else updatedUser.name.value,
                        emailInput = if (isEditingProfile) emailInput else updatedUser.email.value,
                    )
                }
            },
            onError = { setState { copy(error = getErrorMessage("Failed to load profile", it)) } },
        )
    }

    fun onSaveProfileClicked() {
        if (!validateProfileForm() || !currentState.isSaveProfileEnabled) return
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
        val request = UpdateProfileRequest(nameToUpdate, emailToUpdate, null)

        launchWithResult(
            onStart = { copy(actionState = UserProfileUiState.ActionState.UPDATING_PROFILE) },
            onFinally = { copy(actionState = UserProfileUiState.ActionState.IDLE) },
            block = { userApi.updateUserProfile(user.id.value, request) },
            onSuccess = { updatedUserResponse ->
                val updatedUser = updatedUserResponse.toUserInfo()
                sendEvent(UserProfileEvent.ProfileUpdated(updatedUser))
                setState {
                    copy(
                        isEditingProfile = false,
                        userDetails = updatedUser,
                        nameInput = updatedUser.name.value,
                        emailInput = updatedUser.email.value,
                    )
                }
                sendEvent(UserProfileEvent.ShowSnackbar("Profile updated successfully"))
            },
            onError = { handleErrorWithMessage(getErrorMessage("Failed to update profile", it)) },
        )
    }

    fun onChangePasswordClicked() {
        if (!validatePasswordChangeForm() || !currentState.isChangePasswordEnabled) return
        if (currentState.newPasswordInput == currentState.oldPasswordInput) {
            sendEvent(
                UserProfileEvent.ShowSnackbar("New password cannot be the same as the old one."),
            )
            return
        }
        val request =
            ChangePasswordRequest(currentState.oldPasswordInput, currentState.newPasswordInput)

        launchWithResult(
            onStart = { copy(actionState = UserProfileUiState.ActionState.CHANGING_PASSWORD) },
            onFinally = { copy(actionState = UserProfileUiState.ActionState.IDLE) },
            block = { userApi.changePassword(user.id.value, request) },
            onSuccess = {
                setState { copy(showPasswordChangeSection = false) }
                sendEvent(UserProfileEvent.ShowSnackbar("Password changed successfully"))
            },
            onError = { handleErrorWithMessage(getErrorMessage("Failed to change password", it)) },
        )
    }

    fun onDeleteAccountClicked() {
        if (!currentState.isDeleteAccountEnabled) return

        launchWithResult(
            onStart = { copy(actionState = UserProfileUiState.ActionState.DELETING_ACCOUNT) },
            block = { userApi.deleteUser(user.id.value) },
            onSuccess = {
                sendEvent(UserProfileEvent.ShowSnackbar("Account deleted successfully"))
                sendEvent(UserProfileEvent.AccountDeleted)
            },
            onError = { handleErrorWithMessage(getErrorMessage("Failed to delete account", it)) },
        )
    }

    private fun validateProfileForm(): Boolean {
        val nameError =
            try {
                Name(currentState.nameInput)
                null
            } catch (e: IllegalArgumentException) {
                e.message
            }
        val emailError =
            try {
                Email(currentState.emailInput)
                null
            } catch (e: IllegalArgumentException) {
                e.message
            }
        setState { copy(nameInputError = nameError, emailInputError = emailError) }
        return !currentState.hasProfileErrors
    }

    private fun validatePasswordChangeForm(): Boolean {
        val newPasswordError =
            try {
                Password(currentState.newPasswordInput)
                null
            } catch (e: IllegalArgumentException) {
                e.message
            }
        val confirmPasswordError =
            if (newPasswordError == null &&
                currentState.newPasswordInput != currentState.confirmPasswordInput
            ) {
                "Passwords do not match"
            } else {
                null
            }
        setState {
            copy(
                newPasswordInputError = newPasswordError,
                confirmPasswordInputError = confirmPasswordError,
            )
        }
        return !currentState.hasPasswordErrors
    }
}
