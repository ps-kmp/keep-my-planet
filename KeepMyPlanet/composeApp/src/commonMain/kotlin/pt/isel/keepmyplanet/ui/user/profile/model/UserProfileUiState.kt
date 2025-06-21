package pt.isel.keepmyplanet.ui.user.profile.model

data class UserProfileUiState(
    val userDetails: UserInfo? = null,
    val nameInput: String = "",
    val emailInput: String = "",
    val oldPasswordInput: String = "",
    val newPasswordInput: String = "",
    val confirmPasswordInput: String = "",
    val isLoading: Boolean = false,
    val isUpdatingProfile: Boolean = false,
    val isChangingPassword: Boolean = false,
    val isDeletingAccount: Boolean = false,
    val isEditingProfile: Boolean = false,
    val showPasswordChangeSection: Boolean = false,
    val nameInputError: String? = null,
    val emailInputError: String? = null,
    val newPasswordInputError: String? = null,
    val confirmPasswordInputError: String? = null,
    val error: String? = null,
) {
    val hasProfileChanges: Boolean
        get() =
            userDetails != null &&
                (nameInput != userDetails.name.value || emailInput != userDetails.email.value)

    val isSaveProfileEnabled: Boolean
        get() = isEditingProfile && !isUpdatingProfile && hasProfileChanges

    val isChangePasswordEnabled: Boolean
        get() =
            showPasswordChangeSection &&
                !isChangingPassword &&
                oldPasswordInput.isNotBlank() &&
                newPasswordInput.isNotBlank() &&
                confirmPasswordInput.isNotBlank()

    val isDeleteAccountEnabled: Boolean
        get() = !isDeletingAccount && userDetails != null

    val hasProfileErrors: Boolean
        get() = nameInputError != null || emailInputError != null

    val hasPasswordErrors: Boolean
        get() = newPasswordInputError != null || confirmPasswordInputError != null
}
