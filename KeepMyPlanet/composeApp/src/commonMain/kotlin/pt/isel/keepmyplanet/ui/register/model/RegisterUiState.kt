package pt.isel.keepmyplanet.ui.register.model

data class RegisterUiState(
    val username: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
) {
    val isRegisterEnabled: Boolean
        get() =
            username.isNotBlank() &&
                email.isNotBlank() &&
                password.isNotBlank() &&
                password.length >= 8 &&
                confirmPassword.isNotBlank() &&
                password == confirmPassword &&
                !isLoading
    val showPasswordMismatchError: Boolean
        get() = password.isNotEmpty() && confirmPassword.isNotEmpty() && password != confirmPassword
}
