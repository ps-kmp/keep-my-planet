package pt.isel.keepmyplanet.ui.stats

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import pt.isel.keepmyplanet.data.api.EventApi
import pt.isel.keepmyplanet.data.api.UserApi
import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.mapper.event.toListItem
import pt.isel.keepmyplanet.mapper.user.toDomain
import pt.isel.keepmyplanet.ui.stats.states.UserStatsEvent
import pt.isel.keepmyplanet.ui.stats.states.UserStatsUiState
import pt.isel.keepmyplanet.ui.viewmodel.BaseViewModel

class UserStatsViewModel(
    private val userApi: UserApi,
    private val eventApi: EventApi,
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
                        val statsJob = async { userApi.getUserStats(userId.value) }
                        val eventsJob =
                            async {
                                eventApi.getAttendedEvents(limit = currentState.limit, offset = 0)
                            }
                        Pair(statsJob.await(), eventsJob.await())
                    }

                val stats = statsResult.getOrNull()?.toDomain()
                val events = eventsResult.getOrNull()?.map { it.toListItem() } ?: emptyList()

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
                        stats = stats,
                        attendedEvents = events,
                        offset = events.size,
                        hasMorePages = events.size == this.limit,
                        error = errorMessage,
                    )
                }
            } catch (e: Exception) {
                setState { copy(error = getErrorMessage("Failed to load initial data", e)) }
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
            block = {
                eventApi.getAttendedEvents(
                    limit = currentState.limit,
                    offset = currentState.offset,
                )
            },
            onSuccess = { response ->
                setState {
                    val newEvents = response.map { it.toListItem() }
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
