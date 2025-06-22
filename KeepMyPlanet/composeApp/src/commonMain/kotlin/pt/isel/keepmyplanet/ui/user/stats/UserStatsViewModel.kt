package pt.isel.keepmyplanet.ui.user.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pt.isel.keepmyplanet.data.api.EventApi
import pt.isel.keepmyplanet.data.http.ApiException
import pt.isel.keepmyplanet.data.mapper.toListItem
import pt.isel.keepmyplanet.ui.user.stats.model.UserStatsEvent
import pt.isel.keepmyplanet.ui.user.stats.model.UserStatsUiState

class UserStatsViewModel(
    private val eventApi: EventApi,
) : ViewModel() {
    private val _uiState = MutableStateFlow(UserStatsUiState())
    val uiState: StateFlow<UserStatsUiState> = _uiState.asStateFlow()

    private val _events = Channel<UserStatsEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init {
        loadAttendedEvents(isRefresh = true)
    }

    fun loadAttendedEvents(isRefresh: Boolean = false) {
        val currentState = _uiState.value
        val offset = if (isRefresh) 0 else currentState.offset

        if (isRefresh.not() && (currentState.isLoading || currentState.isAddingMore)) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = isRefresh,
                    isAddingMore = !isRefresh,
                    error = if (isRefresh) null else it.error,
                )
            }

            eventApi
                .getAttendedEvents(limit = currentState.limit, offset = offset)
                .onSuccess { response ->
                    _uiState.update {
                        val currentEvents = if (isRefresh) emptyList() else it.attendedEvents
                        val newEvents = response.map { e -> e.toListItem() }
                        it.copy(
                            isLoading = false,
                            isAddingMore = false,
                            attendedEvents = (currentEvents + newEvents).distinctBy { e -> e.id },
                            offset = if (isRefresh) newEvents.size else it.offset + newEvents.size,
                            hasMorePages = newEvents.size == it.limit,
                        )
                    }
                }.onFailure { error ->
                    _uiState.update { it.copy(isLoading = false, isAddingMore = false) }
                    if (isRefresh) {
                        val errorMessage = getErrorMessage("Failed to load stats", error)
                        _uiState.update { it.copy(error = errorMessage) }
                    } else {
                        handleError("Failed to load more events", error)
                    }
                }
        }
    }

    fun loadNextPage() {
        val currentState = _uiState.value
        if (currentState.isLoading || currentState.isAddingMore || !currentState.hasMorePages) {
            return
        }
        loadAttendedEvents()
    }

    private fun getErrorMessage(
        prefix: String,
        error: Throwable,
    ): String =
        when (error) {
            is ApiException -> error.error.message
            else -> "$prefix: ${error.message ?: "Unknown error"}"
        }

    private suspend fun handleError(
        prefix: String,
        error: Throwable,
    ) {
        val message = getErrorMessage(prefix, error)
        _events.send(UserStatsEvent.ShowSnackbar(message))
    }
}
