package pt.isel.keepmyplanet.ui.login.states

import pt.isel.keepmyplanet.ui.base.UiState

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val actionState: ActionState = ActionState.Idle,
    val emailError: String? = null,
    val passwordError: String? = null,
    val apiError: String? = null,
) : UiState {
    sealed interface ActionState {
        data object Idle : ActionState

        data object LoggingIn : ActionState
    }

    val isLoginEnabled: Boolean
        get() = email.isNotBlank() && password.isNotBlank() && actionState == ActionState.Idle

    val hasError: Boolean
        get() = emailError != null || passwordError != null || apiError != null
}
