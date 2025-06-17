package pt.isel.keepmyplanet.ui.event.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pt.isel.keepmyplanet.data.api.EventApi
import pt.isel.keepmyplanet.data.http.ApiException
import pt.isel.keepmyplanet.data.mapper.toListItem
import pt.isel.keepmyplanet.ui.event.list.model.EventFilterType
import pt.isel.keepmyplanet.ui.event.list.model.EventListScreenEvent
import pt.isel.keepmyplanet.ui.event.list.model.EventListUiState

private const val SEARCH_DEBOUNCE_DELAY_MS = 500L

class EventListViewModel(
    private val eventApi: EventApi,
) : ViewModel() {
    private val _listUiState = MutableStateFlow(EventListUiState())
    val listUiState: StateFlow<EventListUiState> = _listUiState.asStateFlow()

    private val _events = Channel<EventListScreenEvent>(Channel.BUFFERED)
    val events: Flow<EventListScreenEvent> = _events.receiveAsFlow()

    private var searchJob: Job? = null

    init {
        refreshEvents()
    }

    fun refreshEvents() {
        _listUiState.update { it.copy(error = null) }
        loadEvents(isRefresh = true)
    }

    fun loadNextPage() {
        val currentState = _listUiState.value
        if (currentState.isLoading || currentState.isAddingMore || !currentState.hasMorePages) {
            return
        }
        loadEvents()
    }

    fun onSearchQueryChanged(query: String) {
        _listUiState.update { it.copy(query = query) }
        searchJob?.cancel()
        searchJob =
            viewModelScope.launch {
                delay(SEARCH_DEBOUNCE_DELAY_MS)
                loadEvents(isRefresh = true)
            }
    }

    fun onFilterChanged(filterType: EventFilterType) {
        if (_listUiState.value.filter == filterType) return
        _listUiState.update { it.copy(filter = filterType, query = "") }
        loadEvents(isRefresh = true)
    }

    private fun loadEvents(isRefresh: Boolean = false) {
        val currentState = _listUiState.value
        val offset = if (isRefresh) 0 else currentState.offset

        if (isRefresh.not() && (currentState.isLoading || currentState.isAddingMore)) return

        viewModelScope.launch {
            _listUiState.update { it.copy(isLoading = isRefresh, isAddingMore = !isRefresh) }

            val result =
                when (currentState.filter) {
                    EventFilterType.ALL ->
                        eventApi.searchAllEvents(
                            currentState.query.ifBlank { null },
                            currentState.limit,
                            offset,
                        )

                    EventFilterType.ORGANIZED ->
                        eventApi.searchOrganizedEvents(
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

            result
                .onSuccess { newEvents ->
                    _listUiState.update {
                        val currentEvents = if (isRefresh) emptyList() else it.events
                        it.copy(
                            events =
                                (currentEvents + newEvents.map { r -> r.toListItem() })
                                    .distinctBy { item -> item.id },
                            offset = if (isRefresh) newEvents.size else it.offset + newEvents.size,
                            error = null,
                            hasMorePages = newEvents.size == it.limit,
                        )
                    }
                }.onFailure { error ->
                    handleError("Failed to load events", error)
                }

            _listUiState.update { it.copy(isLoading = false, isAddingMore = false) }
        }
    }

    private suspend fun handleError(
        prefix: String,
        error: Throwable,
    ) {
        val message =
            when (error) {
                is ApiException -> error.error.message
                else -> "$prefix: ${error.message ?: "Unknown error"}"
            }
        if (listUiState.value.events.isEmpty()) {
            _listUiState.update { it.copy(error = message) }
        } else {
            _events.send(EventListScreenEvent.ShowSnackbar(message))
        }
    }
}
