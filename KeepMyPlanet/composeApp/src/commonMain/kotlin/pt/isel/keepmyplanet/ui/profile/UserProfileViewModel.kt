package pt.isel.keepmyplanet.ui.profile

import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import pt.isel.keepmyplanet.data.repository.PhotoApiRepository
import pt.isel.keepmyplanet.data.repository.UserApiRepository
import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.domain.user.Email
import pt.isel.keepmyplanet.domain.user.Name
import pt.isel.keepmyplanet.domain.user.Password
import pt.isel.keepmyplanet.domain.user.UserInfo
import pt.isel.keepmyplanet.dto.auth.ChangePasswordRequest
import pt.isel.keepmyplanet.dto.user.UpdateProfileRequest
import pt.isel.keepmyplanet.session.SessionManager
import pt.isel.keepmyplanet.ui.base.BaseViewModel
import pt.isel.keepmyplanet.ui.profile.states.UserProfileEvent
import pt.isel.keepmyplanet.ui.profile.states.UserProfileUiState
import pt.isel.keepmyplanet.utils.AppError
import pt.isel.keepmyplanet.utils.ErrorHandler

class UserProfileViewModel(
    private val userRepository: UserApiRepository,
    private val photoRepository: PhotoApiRepository,
    private val sessionManager: SessionManager,
) : BaseViewModel<UserProfileUiState>(UserProfileUiState(), sessionManager) {
    private val user: UserInfo
        get() =
            sessionManager.userSession.value?.userInfo
                ?: throw IllegalStateException("UserProfileViewModel requires a logged-in user.")

    init {
        setState { copy(userDetails = user) }
        loadUserProfile()
        viewModelScope.launch {
            sessionManager.userSession.collectLatest { session ->
                val sessionUser = session?.userInfo
                if (sessionUser != null && sessionUser != currentState.userDetails) {
                    setState { copy(userDetails = sessionUser) }
                    sessionUser.profilePictureId?.let { fetchPhotoUrl(it) }
                }
            }
        }
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
            copy(newPasswordInput = password, newPasswordInputError = null, passwordApiError = null)
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
            block = { userRepository.getUserDetails(user.id) },
            onSuccess = { updatedUser ->
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
                updatedUser.profilePictureId?.let { fetchPhotoUrl(it) }
            },
            onError = { setState { copy(error = getErrorMessage("Failed to load profile", it)) } },
        )
    }

    private fun fetchPhotoUrl(photoId: Id) {
        viewModelScope.launch {
            photoRepository
                .getPhotoUrl(photoId)
                .onSuccess { photoUrl -> setState { copy(photoUrl = photoUrl) } }
                .onFailure { handleErrorWithMessage("Could not load profile picture.") }
        }
    }

    fun onProfilePictureSelected(
        imageData: ByteArray,
        filename: String,
    ) {
        if (!currentState.isEditingProfile) return

        viewModelScope.launch {
            setState { copy(actionState = UserProfileUiState.ActionState.UPDATING_PHOTO) }
            photoRepository
                .createPhoto(imageData, filename)
                .onSuccess { photoResponse ->
                    val photoId = Id(photoResponse.id)
                    val request = UpdateProfileRequest(profilePictureId = photoId.value)
                    performProfileUpdate(request, isPhotoUpdate = true)
                }.onFailure {
                    handleErrorWithMessage(getErrorMessage("Failed to upload photo", it))
                    setState { copy(actionState = UserProfileUiState.ActionState.IDLE) }
                }
        }
    }

    fun onSaveProfileClicked() {
        if (!validateProfileForm() || !currentState.isSaveProfileEnabled) return

        val nameToUpdate =
            currentState.nameInput.takeIf {
                it !=
                    currentState.userDetails?.name?.value
            }
        val emailToUpdate =
            currentState.emailInput.takeIf {
                it !=
                    currentState.userDetails?.email?.value
            }

        if (nameToUpdate == null && emailToUpdate == null) {
            setState { copy(isEditingProfile = false) }
            return
        }

        val request = UpdateProfileRequest(nameToUpdate, emailToUpdate)
        performProfileUpdate(request, isPhotoUpdate = false)
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
            block = { userRepository.changePassword(user.id, request) },
            onSuccess = {
                setState {
                    copy(
                        showPasswordChangeSection = false,
                        oldPasswordInput = "",
                        newPasswordInput = "",
                        confirmPasswordInput = "",
                    )
                }
                sendEvent(UserProfileEvent.ShowSnackbar("Password changed successfully"))
            },
            onError = { throwable ->
                val appError = ErrorHandler.map(throwable)

                when (appError) {
                    is AppError.GeneralError -> {
                        handleErrorWithMessage(appError.message)
                    }

                    is AppError.ApiFormError -> {
                        setState { copy(passwordApiError = appError.message) }
                    }
                }
            },
        )
    }

    fun onDeleteAccountClicked() {
        if (!currentState.isDeleteAccountEnabled) return

        launchWithResult(
            onStart = { copy(actionState = UserProfileUiState.ActionState.DELETING_ACCOUNT) },
            onFinally = { copy(actionState = UserProfileUiState.ActionState.IDLE) },
            block = { userRepository.deleteUser(user.id) },
            onSuccess = {
                sendEvent(UserProfileEvent.ShowSnackbar("Account deleted successfully"))
                sendEvent(UserProfileEvent.AccountDeleted)
            },
            onError = { handleErrorWithMessage(getErrorMessage("Failed to delete account", it)) },
        )
    }

    private fun performProfileUpdate(
        request: UpdateProfileRequest,
        isPhotoUpdate: Boolean,
    ) {
        launchWithResult(
            onStart = {
                copy(
                    actionState =
                        if (isPhotoUpdate) {
                            UserProfileUiState.ActionState.UPDATING_PHOTO
                        } else {
                            UserProfileUiState.ActionState.UPDATING_PROFILE
                        },
                )
            },
            onFinally = { copy(actionState = UserProfileUiState.ActionState.IDLE) },
            block = { userRepository.updateUserProfile(user.id, request) },
            onSuccess = { updatedUser ->
                sendEvent(UserProfileEvent.ProfileUpdated(updatedUser))
                sendEvent(UserProfileEvent.ShowSnackbar("Profile updated successfully"))
            },
            onError = { error ->
                if (isPhotoUpdate) {
                    handleErrorWithMessage(getErrorMessage("Failed to update profile", error))
                    return@launchWithResult
                }

                val errorMessage = error.message ?: "An unknown error occurred"

                when {
                    errorMessage.contains("Username", ignoreCase = true) ||
                        errorMessage.contains("name", ignoreCase = true) -> {
                        setState { copy(nameInputError = errorMessage) }
                    }

                    errorMessage.contains("Email", ignoreCase = true) -> {
                        setState { copy(emailInputError = errorMessage) }
                    }

                    else -> {
                        handleErrorWithMessage(getErrorMessage("Failed to update profile", error))
                    }
                }
            },
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
