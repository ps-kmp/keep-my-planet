package pt.isel.keepmyplanet.ui.login

import pt.isel.keepmyplanet.data.repository.AuthApiRepository
import pt.isel.keepmyplanet.domain.user.Email
import pt.isel.keepmyplanet.domain.user.Password
import pt.isel.keepmyplanet.dto.auth.LoginRequest
import pt.isel.keepmyplanet.mapper.user.toUserSession
import pt.isel.keepmyplanet.ui.base.BaseViewModel
import pt.isel.keepmyplanet.ui.login.states.LoginEvent
import pt.isel.keepmyplanet.ui.login.states.LoginUiState
import pt.isel.keepmyplanet.utils.AppError
import pt.isel.keepmyplanet.utils.ErrorHandler

class LoginViewModel(
    private val authRepository: AuthApiRepository,
) : BaseViewModel<LoginUiState>(LoginUiState()) {
    override fun handleErrorWithMessage(message: String) {
        sendEvent(LoginEvent.ShowSnackbar(message))
    }

    fun onEmailChanged(email: String) {
        val emailError =
            try {
                Email(email)
                null
            } catch (e: IllegalArgumentException) {
                e.message
            }
        setState { copy(email = email, emailError = emailError, apiError = null) }
    }

    fun onPasswordChanged(password: String) {
        val passwordError =
            if (password.isNotBlank()) null else "Password cannot be blank."
        setState { copy(password = password, passwordError = passwordError, apiError = null) }
    }

    fun onLoginClicked() {
        if (!validateForm() || !currentState.isLoginEnabled) return

        val request = LoginRequest(currentState.email.trim(), currentState.password)

        launchWithResult(
            onStart = {
                copy(actionState = LoginUiState.ActionState.LoggingIn, apiError = null)
            },
            onFinally = { copy(actionState = LoginUiState.ActionState.Idle) },
            block = {
                authRepository.login(request)
            },
            onSuccess = { loginResponse ->
                sendEvent(LoginEvent.LoginSuccess(loginResponse.toUserSession()))
            },
            onError = { throwable ->
                val appError = ErrorHandler.map(throwable)
                when (appError) {
                    is AppError.ApiFormError -> {
                        setState { copy(apiError = appError.message) }
                    }
                    is AppError.GeneralError -> {
                        handleErrorWithMessage(appError.message)
                    }
                }
            },
        )
    }

    private fun validateForm(): Boolean {
        val emailError =
            try {
                Email(currentState.email)
                null
            } catch (e: IllegalArgumentException) {
                e.message
            }

        val passwordError =
            try {
                Password(currentState.password)
                null
            } catch (e: IllegalArgumentException) {
                e.message
            }
        setState { copy(emailError = emailError, passwordError = passwordError) }
        return emailError == null && passwordError == null
    }
}
