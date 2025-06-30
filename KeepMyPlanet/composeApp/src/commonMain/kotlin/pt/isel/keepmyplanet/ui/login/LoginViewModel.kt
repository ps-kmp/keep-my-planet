package pt.isel.keepmyplanet.ui.login

import pt.isel.keepmyplanet.data.repository.DefaultAuthRepository
import pt.isel.keepmyplanet.domain.user.Email
import pt.isel.keepmyplanet.dto.auth.LoginRequest
import pt.isel.keepmyplanet.mapper.user.toUserSession
import pt.isel.keepmyplanet.ui.base.BaseViewModel
import pt.isel.keepmyplanet.ui.login.states.LoginEvent
import pt.isel.keepmyplanet.ui.login.states.LoginUiState

class LoginViewModel(
    private val authRepository: DefaultAuthRepository,
) : BaseViewModel<LoginUiState>(LoginUiState()) {
    override fun handleErrorWithMessage(message: String) {
        sendEvent(LoginEvent.ShowSnackbar(message))
    }

    fun onEmailChanged(email: String) {
        setState { copy(email = email, emailError = null) }
    }

    fun onPasswordChanged(password: String) {
        setState { copy(password = password) }
    }

    fun onLoginClicked() {
        if (!validateForm() || !currentState.isLoginEnabled) return

        val request = LoginRequest(currentState.email.trim(), currentState.password)

        launchWithResult(
            onStart = { copy(actionState = LoginUiState.ActionState.LoggingIn) },
            onFinally = { copy(actionState = LoginUiState.ActionState.Idle) },
            block = { authRepository.login(request) },
            onSuccess = { sendEvent(LoginEvent.LoginSuccess(it.toUserSession())) },
            onError = { handleErrorWithMessage(getErrorMessage("Login failed", it)) },
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
        setState { copy(emailError = emailError) }
        return !currentState.hasError
    }
}
