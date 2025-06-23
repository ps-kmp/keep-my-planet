package pt.isel.keepmyplanet.ui.event.list

import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import pt.isel.keepmyplanet.data.api.EventApi
import pt.isel.keepmyplanet.data.mapper.toListItem
import pt.isel.keepmyplanet.dto.event.EventResponse
import pt.isel.keepmyplanet.ui.base.BaseViewModel
import pt.isel.keepmyplanet.ui.event.list.model.EventFilterType
import pt.isel.keepmyplanet.ui.event.list.model.EventListEvent
import pt.isel.keepmyplanet.ui.event.list.model.EventListUiState

private const val SEARCH_DEBOUNCE_DELAY_MS = 500L

class EventListViewModel(
    private val eventApi: EventApi,
) : BaseViewModel<EventListUiState>(EventListUiState()) {
    private var searchJob: Job? = null

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
        loadEvents(isRefresh = true)
    }

    fun loadNextPage() {
        val state = currentState
        if (state.isLoading || state.isAddingMore || !state.hasMorePages) {
            return
        }
        loadEvents()
    }

    fun onSearchQueryChanged(query: String) {
        setState { copy(query = query) }
        searchJob?.cancel()
        searchJob =
            viewModelScope.launch {
                delay(SEARCH_DEBOUNCE_DELAY_MS)
                loadEvents(isRefresh = true)
            }
    }

    fun onFilterChanged(filterType: EventFilterType) {
        if (currentState.filter == filterType) return
        setState { copy(filter = filterType) }
        loadEvents(isRefresh = true)
    }

    private fun loadEvents(isRefresh: Boolean = false) {
        val offset = if (isRefresh) 0 else currentState.offset

        if (isRefresh.not() && (currentState.isLoading || currentState.isAddingMore)) return

        val block: suspend () -> Result<List<*>> = {
            when (currentState.filter) {
                EventFilterType.ALL ->
                    eventApi.searchAllEvents(
                        currentState.query.ifBlank { null },
                        currentState.limit,
                        offset,
                    )

                EventFilterType.ORGANIZED ->
                    eventApi
                        .searchOrganizedEvents(
                            currentState.query.ifBlank { null },
                            currentState.limit,
                            offset,
                        )

                EventFilterType.JOINED ->
                    eventApi.searchJoinedEvents(
                        currentState.query.ifBlank { null },
                        currentState.limit,
                        offset,
                    )
            }
        }

        launchWithResult(
            onStart = { copy(isLoading = isRefresh, isAddingMore = !isRefresh) },
            onFinally = { copy(isLoading = false, isAddingMore = false) },
            block = block,
            onSuccess = { newEvents ->
                setState {
                    val currentEvents = if (isRefresh) emptyList() else this.events
                    copy(
                        events =
                            (currentEvents + newEvents.map { (it as EventResponse).toListItem() })
                                .distinctBy { it.id },
                        offset = if (isRefresh) newEvents.size else this.offset + newEvents.size,
                        error = null,
                        hasMorePages = newEvents.size == this.limit,
                    )
                }
            },
            onError = { handleErrorWithMessage(getErrorMessage("Failed to load events", it)) },
        )
    }
}
