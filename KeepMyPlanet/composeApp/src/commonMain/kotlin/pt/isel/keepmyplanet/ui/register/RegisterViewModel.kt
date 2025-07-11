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
        val usernameError =
            try {
                Name(username)
                null
            } catch (e: IllegalArgumentException) {
                e.message
            }
        setState { copy(username = username, usernameError = usernameError) }
    }

    fun onEmailChanged(email: String) {
        val emailError =
            try {
                Email(email)
                null
            } catch (e: IllegalArgumentException) {
                e.message
            }
        setState { copy(email = email, emailError = emailError) }
    }

    fun onPasswordChanged(password: String) {
        val passwordError =
            try {
                Password(password)
                null
            } catch (e: IllegalArgumentException) {
                e.message
            }
        val confirmError =
            if (currentState.confirmPassword.isNotEmpty() &&
                password != currentState.confirmPassword
            ) {
                "Passwords do not match"
            } else {
                null
            }
        setState {
            copy(
                password = password,
                passwordError = passwordError,
                confirmPasswordError = confirmError,
            )
        }
    }

    fun onConfirmPasswordChanged(confirmPassword: String) {
        val confirmError =
            if (confirmPassword != currentState.password) {
                "Passwords do not match"
            } else {
                null
            }
        setState { copy(confirmPassword = confirmPassword, confirmPasswordError = confirmError) }
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
        onUsernameChanged(currentState.username)
        onEmailChanged(currentState.email)
        onPasswordChanged(currentState.password)
        onConfirmPasswordChanged(currentState.confirmPassword)
        return !currentState.hasErrors
    }
}
