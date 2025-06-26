package pt.isel.keepmyplanet.ui.profile.states

import pt.isel.keepmyplanet.domain.user.UserInfo
import pt.isel.keepmyplanet.ui.viewmodel.UiState

data class UserProfileUiState(
    val userDetails: UserInfo? = null,
    val nameInput: String = "",
    val emailInput: String = "",
    val oldPasswordInput: String = "",
    val newPasswordInput: String = "",
    val confirmPasswordInput: String = "",
    val isLoading: Boolean = false,
    val actionState: ActionState = ActionState.IDLE,
    val isEditingProfile: Boolean = false,
    val showPasswordChangeSection: Boolean = false,
    val nameInputError: String? = null,
    val emailInputError: String? = null,
    val newPasswordInputError: String? = null,
    val confirmPasswordInputError: String? = null,
    val photoUrl: String? = null,
    val error: String? = null,
) : UiState {
    enum class ActionState {
        IDLE,
        UPDATING_PROFILE,
        CHANGING_PASSWORD,
        DELETING_ACCOUNT,
        UPDATING_PHOTO,
    }

    val isUpdatingPhoto: Boolean
        get() = actionState == ActionState.UPDATING_PHOTO

    val hasProfileChanges: Boolean
        get() =
            userDetails != null &&
                (nameInput != userDetails.name.value || emailInput != userDetails.email.value)

    val isSaveProfileEnabled: Boolean
        get() = isEditingProfile && actionState == ActionState.IDLE && hasProfileChanges

    val isChangePasswordEnabled: Boolean
        get() =
            showPasswordChangeSection &&
                actionState == ActionState.IDLE &&
                oldPasswordInput.isNotBlank() &&
                newPasswordInput.isNotBlank() &&
                confirmPasswordInput.isNotBlank()

    val isDeleteAccountEnabled: Boolean
        get() = actionState == ActionState.IDLE && userDetails != null

    val hasProfileErrors: Boolean
        get() = nameInputError != null || emailInputError != null

    val hasPasswordErrors: Boolean
        get() = newPasswordInputError != null || confirmPasswordInputError != null
}
