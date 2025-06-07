package pt.isel.keepmyplanet.ui.login.model

data class LoginUiState(
    val username: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
) {
    val isLoginEnabled: Boolean
        get() = username.isNotBlank() && password.isNotBlank() && !isLoading
}
