package pt.isel.keepmyplanet.ui.register

import pt.isel.keepmyplanet.data.api.UserApi
import pt.isel.keepmyplanet.domain.user.Email
import pt.isel.keepmyplanet.domain.user.Name
import pt.isel.keepmyplanet.domain.user.Password
import pt.isel.keepmyplanet.dto.user.RegisterRequest
import pt.isel.keepmyplanet.ui.base.BaseViewModel
import pt.isel.keepmyplanet.ui.register.model.RegisterEvent
import pt.isel.keepmyplanet.ui.register.model.RegisterUiState

class RegisterViewModel(
    private val userApi: UserApi,
) : BaseViewModel<RegisterUiState>(RegisterUiState()) {
    override fun handleErrorWithMessage(message: String) {
        sendEvent(RegisterEvent.ShowSnackbar(message))
    }

    fun onUsernameChanged(username: String) {
        setState { copy(username = username, usernameError = null) }
    }

    fun onEmailChanged(email: String) {
        setState { copy(email = email, emailError = null) }
    }

    fun onPasswordChanged(password: String) {
        setState { copy(password = password, passwordError = null, confirmPasswordError = null) }
    }

    fun onConfirmPasswordChanged(confirmPassword: String) {
        setState { copy(confirmPassword = confirmPassword, confirmPasswordError = null) }
    }

    fun onRegisterClicked() {
        if (!validateForm()) return

        val request =
            RegisterRequest(
                name = currentState.username.trim(),
                email = currentState.email.trim(),
                password = currentState.password,
            )

        launchWithResult(
            onStart = { copy(actionState = RegisterUiState.ActionState.Registering) },
            onFinally = { copy(actionState = RegisterUiState.ActionState.Idle) },
            block = { userApi.registerUser(request) },
            onSuccess = {
                sendEvent(RegisterEvent.ShowSnackbar("Registration successful! Please login."))
                sendEvent(RegisterEvent.NavigateToLogin)
            },
            onError = { handleErrorWithMessage(getErrorMessage("Registration failed", it)) },
        )
    }

    private fun validateForm(): Boolean {
        val stateWithErrors =
            currentState.copy(
                usernameError =
                    try {
                        Name(currentState.username)
                        null
                    } catch (e: IllegalArgumentException) {
                        e.message
                    },
                emailError =
                    try {
                        Email(currentState.email)
                        null
                    } catch (e: IllegalArgumentException) {
                        e.message
                    },
                passwordError =
                    try {
                        Password(currentState.password)
                        null
                    } catch (e: IllegalArgumentException) {
                        e.message
                    },
                confirmPasswordError =
                    if (currentState.password != currentState.confirmPassword) {
                        "Passwords do not match"
                    } else {
                        null
                    },
            )

        setState { stateWithErrors }
        return !stateWithErrors.hasErrors
    }
}
