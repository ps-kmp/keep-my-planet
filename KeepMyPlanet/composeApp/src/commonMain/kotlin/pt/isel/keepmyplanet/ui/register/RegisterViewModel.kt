package pt.isel.keepmyplanet.ui.register

import pt.isel.keepmyplanet.data.repository.UserApiRepository
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
    private val userRepository: UserApiRepository,
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
        setState { copy(password = password, passwordError = null) }
    }

    fun onConfirmPasswordChanged(confirmPassword: String) {
        setState { copy(confirmPassword = confirmPassword, confirmPasswordError = null) }
    }

    fun onRegisterClicked() {
        if (!validateForm() || !currentState.canAttemptRegister) return

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
        val state = currentState
        val usernameError =
            try {
                Name(state.username.trim())
                null
            } catch (e: IllegalArgumentException) {
                e.message
            }
        val emailError =
            try {
                Email(state.email.trim())
                null
            } catch (e: IllegalArgumentException) {
                e.message
            }
        val passwordError =
            try {
                Password(state.password)
                null
            } catch (e: IllegalArgumentException) {
                e.message
            }
        val confirmPasswordError =
            if (state.password != state.confirmPassword) {
                "Passwords do not match"
            } else {
                null
            }

        val stateWithErrors =
            state.copy(
                usernameError = usernameError,
                emailError = emailError,
                passwordError = passwordError,
                confirmPasswordError = confirmPasswordError,
            )

        setState { stateWithErrors }
        return !stateWithErrors.hasErrors
    }
}
