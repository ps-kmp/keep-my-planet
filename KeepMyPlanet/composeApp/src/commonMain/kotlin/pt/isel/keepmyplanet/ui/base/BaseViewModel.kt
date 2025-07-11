package pt.isel.keepmyplanet.ui.base

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pt.isel.keepmyplanet.data.http.ApiException
import pt.isel.keepmyplanet.exception.AuthenticationException
import pt.isel.keepmyplanet.session.SessionManager

abstract class BaseViewModel<S : UiState>(
    initialState: S,
    private val sessionManager: SessionManager? = null,
) {
    val viewModelScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    fun onCleared() {
        onViewModelCleared()
        viewModelScope.coroutineContext.cancelChildren()
    }

    protected open fun onViewModelCleared() {
        // Default implementation is empty
    }

    private val _uiState = MutableStateFlow(initialState)
    val uiState: StateFlow<S> = _uiState.asStateFlow()

    private val _events = Channel<UiEvent>(Channel.BUFFERED)
    val events: Flow<UiEvent> = _events.receiveAsFlow()

    protected val currentState: S
        get() = uiState.value

    protected fun setState(reduce: S.() -> S) {
        _uiState.update(reduce)
    }

    protected fun sendEvent(event: UiEvent) {
        viewModelScope.launch {
            _events.send(event)
        }
    }

    protected fun <T> launchWithResult(
        onStart: (S.() -> S)? = null,
        onFinally: (S.() -> S)? = null,
        block: suspend () -> Result<T>,
        onSuccess: suspend (T) -> Unit,
        onError: (Throwable) -> Unit = { handleError(it) },
    ): Job =
        viewModelScope.launch {
            onStart?.let { setState(it) }
            try {
                val result = block()
                if (result.isSuccess) {
                    onSuccess(result.getOrThrow())
                } else {
                    val error = result.exceptionOrNull() ?: Exception("Unknown error in result")
                    if (error is AuthenticationException) {
                        sessionManager?.clearSession()
                        handleErrorWithMessage("Your session has expired. Please log in again.")
                    } else {
                        onError(error)
                    }
                }
            } finally {
                onFinally?.let { setState(it) }
            }
        }

    protected fun getErrorMessage(
        prefix: String,
        error: Throwable,
    ): String =
        when (error) {
            is ApiException -> error.error.message
            else -> "$prefix: ${error.message ?: "Unknown error"}"
        }

    private fun handleError(error: Throwable) {
        val message = getErrorMessage("An unexpected error occurred", error)
        handleErrorWithMessage(message)
    }

    protected abstract fun handleErrorWithMessage(message: String)
}
