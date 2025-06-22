package pt.isel.keepmyplanet.ui.register.model

data class RegisterUiState(
    val username: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val actionState: ActionState = ActionState.Idle,
    val usernameError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
) {
    sealed interface ActionState {
        data object Idle : ActionState

        data object Registering : ActionState
    }

    val canAttemptRegister: Boolean
        get() = actionState == ActionState.Idle

    val hasErrors: Boolean
        get() =
            usernameError != null ||
                emailError != null ||
                passwordError != null ||
                confirmPasswordError != null
}
