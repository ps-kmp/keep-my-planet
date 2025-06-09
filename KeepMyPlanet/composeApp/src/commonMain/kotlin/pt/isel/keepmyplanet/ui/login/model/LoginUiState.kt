package pt.isel.keepmyplanet.ui.login.model

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
) {
    val isLoginEnabled: Boolean
        get() = email.isNotBlank() && password.isNotBlank() && !isLoading
}
