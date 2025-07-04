package pt.isel.keepmyplanet.ui.register

import pt.isel.keepmyplanet.data.repository.DefaultUserRepository
import pt.isel.keepmyplanet.domain.user.Email
import pt.isel.keepmyplanet.domain.user.Name
import pt.isel.keepmyplanet.domain.user.Password
import pt.isel.keepmyplanet.dto.auth.RegisterRequest
import pt.isel.keepmyplanet.ui.base.BaseViewModel
import pt.isel.keepmyplanet.ui.register.states.RegisterEvent
import pt.isel.keepmyplanet.ui.register.states.RegisterUiState
import pt.isel.keepmyplanet.utils.AppError
import pt.isel.keepmyplanet.utils.ErrorHandler

class RegisterViewModel(
    private val userRepository: DefaultUserRepository,
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
            block = { userRepository.registerUser(request) },
            onSuccess = {
                sendEvent(RegisterEvent.ShowSnackbar("Registration successful! Please login."))
                sendEvent(RegisterEvent.NavigateToLogin)
            },
            onError = { throwable ->
                val appError = ErrorHandler.map(throwable)

                when (appError) {
                    is AppError.ApiFormError -> {
                        val errorMessage = appError.message

                        when {
                            errorMessage.contains("Username", ignoreCase = true) -> {
                                setState { copy(usernameError = errorMessage) }
                            }
                            errorMessage.contains("Email", ignoreCase = true) -> {
                                setState { copy(emailError = errorMessage) }
                            }
                            else -> {
                                handleErrorWithMessage(errorMessage)
                            }
                        }
                    }
                    is AppError.GeneralError -> {
                        handleErrorWithMessage(appError.message)
                    }
                }
            },
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
