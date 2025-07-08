package pt.isel.keepmyplanet.ui.event.list

import androidx.compose.foundation.lazy.LazyListState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import pt.isel.keepmyplanet.data.repository.DefaultEventRepository
import pt.isel.keepmyplanet.domain.event.EventFilterType
import pt.isel.keepmyplanet.session.SessionManager
import pt.isel.keepmyplanet.ui.base.BaseViewModel
import pt.isel.keepmyplanet.ui.event.list.states.EventListEvent
import pt.isel.keepmyplanet.ui.event.list.states.EventListUiState

private const val SEARCH_DEBOUNCE_DELAY_MS = 500L

class EventListViewModel(
    private val eventRepository: DefaultEventRepository,
    sessionManager: SessionManager,
) : BaseViewModel<EventListUiState>(
        EventListUiState(isGuest = sessionManager.userSession.value == null),
        sessionManager,
    ) {
    private var searchJob: Job? = null

    val listStates = EventFilterType.entries.associateWith { LazyListState() }

    init {
        refreshEvents()
    }

    override fun handleErrorWithMessage(message: String) {
        if (currentState.events.isEmpty()) {
            setState { copy(error = message) }
        } else {
            sendEvent(EventListEvent.ShowSnackbar(message))
        }
    }

    fun refreshEvents() {
        setState { copy(error = null) }
        loadEvents(isRefresh = true, clearPrevious = false)
    }

    fun loadNextPage() {
        val state = currentState
        if (state.isLoading || state.isAddingMore || !state.hasMorePages) {
            return
        }
        loadEvents(isRefresh = false, clearPrevious = false)
    }

    fun onSearchQueryChanged(query: String) {
        setState { copy(query = query) }
        searchJob?.cancel()
        searchJob =
            viewModelScope.launch {
                delay(SEARCH_DEBOUNCE_DELAY_MS)
                loadEvents(isRefresh = true, clearPrevious = true)
            }
    }

    fun onFilterChanged(filterType: EventFilterType) {
        if (currentState.filter == filterType ||
            (currentState.isGuest && filterType != EventFilterType.ALL)
        ) {
            return
        }
        setState { copy(filter = filterType) }
        loadEvents(isRefresh = true, clearPrevious = true)
    }

    private fun loadEvents(
        isRefresh: Boolean,
        clearPrevious: Boolean,
    ) {
        val offset = if (isRefresh) 0 else currentState.offset

        if (isRefresh.not() && (currentState.isLoading || currentState.isAddingMore)) return

        launchWithResult(
            onStart = {
                copy(
                    isLoading = isRefresh,
                    isAddingMore = !isRefresh,
                    offset = if (clearPrevious) 0 else this.offset,
                    hasMorePages = if (clearPrevious) true else this.hasMorePages,
                )
            },
            onFinally = { copy(isLoading = false, isAddingMore = false) },
            block = {
                eventRepository.searchEvents(
                    filter = currentState.filter,
                    query = currentState.query.ifBlank { null },
                    limit = currentState.limit,
                    offset = offset,
                )
            },
            onSuccess = { newEvents ->
                setState {
                    val currentEvents = if (isRefresh) emptyList() else this.events
                    copy(
                        events = (currentEvents + newEvents).distinctBy { it.id },
                        offset = if (isRefresh) 0 else this.offset + newEvents.size,
                        hasMorePages = newEvents.size == this.limit,
                        error = null,
                    )
                }
            },
            onError = {
                if (currentState.events.isEmpty()) {
                    handleErrorWithMessage(getErrorMessage("Failed to load events", it))
                } else {
                    sendEvent(
                        EventListEvent.ShowSnackbar(
                            getErrorMessage("Failed to refresh events", it),
                        ),
                    )
                }
            },
        )
    }
}
