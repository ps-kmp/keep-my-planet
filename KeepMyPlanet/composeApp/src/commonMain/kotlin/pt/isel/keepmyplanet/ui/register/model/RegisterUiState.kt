package pt.isel.keepmyplanet.ui.register.model

data class RegisterUiState(
    val username: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    // Validation Errors
    val usernameError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
) {
    val canAttemptRegister: Boolean
        get() = !isLoading

    val hasErrors: Boolean
        get() =
            usernameError != null ||
                emailError != null ||
                passwordError != null ||
                confirmPasswordError != null
}
