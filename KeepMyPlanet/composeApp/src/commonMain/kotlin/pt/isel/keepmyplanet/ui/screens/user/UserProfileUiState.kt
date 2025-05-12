package pt.isel.keepmyplanet.ui.screens.user

import pt.isel.keepmyplanet.data.model.UserInfo

data class UserProfileUiState(
    val userDetails: UserInfo? = null,
    // Input fields for editing
    val nameInput: String = "",
    val emailInput: String = "",
    // Input fields for password change
    val oldPasswordInput: String = "",
    val newPasswordInput: String = "",
    val confirmPasswordInput: String = "",
    // UI Control Flags
    val isLoading: Boolean = false,
    val isUpdatingProfile: Boolean = false,
    val isChangingPassword: Boolean = false,
    val isDeletingAccount: Boolean = false,
    val isEditingProfile: Boolean = false,
    val showPasswordChangeSection: Boolean = false,
) {
    val isSaveProfileEnabled: Boolean
        get() =
            isEditingProfile &&
                !isUpdatingProfile &&
                nameInput.isNotBlank() &&
                emailInput.isNotBlank()

    val isChangePasswordEnabled: Boolean
        get() =
            showPasswordChangeSection &&
                !isChangingPassword &&
                oldPasswordInput.isNotBlank() &&
                newPasswordInput.isNotBlank() &&
                confirmPasswordInput.isNotBlank()

    val isDeleteAccountEnabled: Boolean
        get() = !isDeletingAccount && userDetails != null

    val hasProfileChanges: Boolean
        get() =
            userDetails != null &&
                (nameInput != userDetails.username || emailInput != userDetails.email)
}
