package pt.isel.keepmyplanet.ui.stats

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import pt.isel.keepmyplanet.data.repository.DefaultEventRepository
import pt.isel.keepmyplanet.data.repository.DefaultUserRepository
import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.ui.base.BaseViewModel
import pt.isel.keepmyplanet.ui.stats.states.UserStatsEvent
import pt.isel.keepmyplanet.ui.stats.states.UserStatsUiState

class UserStatsViewModel(
    private val userRepository: DefaultUserRepository,
    private val eventRepository: DefaultEventRepository,
    private val userId: Id,
) : BaseViewModel<UserStatsUiState>(UserStatsUiState()) {
    init {
        loadInitialData()
    }

    override fun handleErrorWithMessage(message: String) {
        sendEvent(UserStatsEvent.ShowSnackbar(message))
    }

    fun loadInitialData() {
        viewModelScope.launch {
            setState { copy(isLoading = true, error = null) }

            try {
                val (statsResult, eventsResult) =
                    coroutineScope {
                        val statsJob = async { userRepository.getUserStats(userId) }
                        val eventsJob =
                            async { eventRepository.getAttendedEvents(currentState.limit, 0) }
                        Pair(statsJob.await(), eventsJob.await())
                    }

                val stats = statsResult.getOrNull()
                val events = eventsResult.getOrNull() ?: emptyList()

                val errorMessage =
                    if (statsResult.isFailure || eventsResult.isFailure) {
                        val statsError =
                            statsResult.exceptionOrNull()?.let {
                                getErrorMessage("Failed to load stats", it)
                            }
                        val eventsError =
                            eventsResult.exceptionOrNull()?.let {
                                getErrorMessage("Failed to load events", it)
                            }
                        listOfNotNull(
                            statsError,
                            eventsError,
                        ).joinToString("\n").ifEmpty { "Failed to load all data." }
                    } else {
                        null
                    }

                setState {
                    copy(
                        stats = stats ?: this.stats,
                        attendedEvents = events,
                        offset = events.size,
                        hasMorePages = events.size == this.limit,
                        error = if (this.stats == null && events.isEmpty()) errorMessage else null,
                    )
                }

                if (errorMessage != null &&
                    (currentState.stats != null || currentState.attendedEvents.isNotEmpty())
                ) {
                    handleErrorWithMessage("Failed to refresh some data.")
                }
            } catch (e: Exception) {
                if (currentState.stats == null) {
                    setState { copy(error = getErrorMessage("Failed to load initial data", e)) }
                }
            } finally {
                setState { copy(isLoading = false) }
            }
        }
    }

    fun loadNextPage() {
        val state = currentState
        if (state.isLoading || state.isAddingMore || !state.hasMorePages) return

        launchWithResult(
            onStart = { copy(isAddingMore = true) },
            onFinally = { copy(isAddingMore = false) },
            block = { eventRepository.getAttendedEvents(currentState.limit, currentState.offset) },
            onSuccess = { newEvents ->
                setState {
                    copy(
                        attendedEvents = (this.attendedEvents + newEvents).distinctBy { it.id },
                        offset = this.offset + newEvents.size,
                        hasMorePages = newEvents.size == this.limit,
                    )
                }
            },
            onError = { handleErrorWithMessage(getErrorMessage("Failed to load more events", it)) },
        )
    }
}
