package pt.isel.keepmyplanet.ui.screens.event

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pt.isel.keepmyplanet.data.model.UserInfo
import pt.isel.keepmyplanet.data.model.toEventInfo
import pt.isel.keepmyplanet.data.service.EventHttpClient
import pt.isel.keepmyplanet.dto.event.CreateEventRequest
import pt.isel.keepmyplanet.dto.event.UpdateEventRequest

class EventViewModel(
    private val eventHttpClient: EventHttpClient,
    private val user: UserInfo,
) : ViewModel() {
    private val _listUiState = MutableStateFlow(EventListUiState())
    val listUiState: StateFlow<EventListUiState> = _listUiState.asStateFlow()

    private val _detailsUiState = MutableStateFlow(EventDetailsUiState())
    val detailsUiState: StateFlow<EventDetailsUiState> = _detailsUiState.asStateFlow()

    private val _events = Channel<EventScreenEvent>(Channel.BUFFERED)
    val events: Flow<EventScreenEvent> = _events.receiveAsFlow()

    init {
        loadEvents()
    }

    fun loadEvents(
        query: String? = _listUiState.value.query.ifBlank { null },
        limit: Int = _listUiState.value.limit,
        offset: Int = _listUiState.value.offset,
    ) {
        viewModelScope.launch {
            _listUiState.update { it.copy(isLoading = true) }
            eventHttpClient
                .searchAllEvents(query, limit, offset)
                .onSuccess { events ->
                    _listUiState.update {
                        it.copy(
                            events = events.map { response -> response.toEventInfo() },
                            isLoading = false,
                            error = null,
                            limit = limit,
                            offset = offset,
                        )
                    }
                }.onFailure {
                    _listUiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Failed to load events",
                        )
                    }
                }
        }
    }

    fun loadNextPage() {
        val currentState = _listUiState.value
        if (currentState.canLoadNext) {
            loadEvents(offset = currentState.offset + currentState.limit)
        }
    }

    fun loadPreviousPage() {
        val currentState = _listUiState.value
        if (currentState.canLoadPrevious) {
            loadEvents(offset = (currentState.offset - currentState.limit).coerceAtLeast(0))
        }
    }

    fun changeLimit(newLimit: Int) {
        if (!_listUiState.value.isLoading && newLimit > 0) {
            loadEvents(limit = newLimit, offset = 0)
        }
    }

    fun createEvent(request: CreateEventRequest) {
        viewModelScope.launch {
            _listUiState.value = _listUiState.value.copy(isLoading = true)
            eventHttpClient
                .createEvent(request)
                .onSuccess { response ->
                    val newEvent = response.toEventInfo()
                    _listUiState.update {
                        it.copy(
                            events = listOf(newEvent) + it.events,
                            isLoading = false,
                        )
                    }
                    _events.send(EventScreenEvent.EventCreated(response.id))
                    _events.send(EventScreenEvent.ShowSnackbar("Event created successfully"))
                }.onFailure {
                    _listUiState.value =
                        _listUiState.value.copy(
                            isLoading = false,
                            error = "Failed to create event",
                        )
                }
        }
    }

    fun loadEventDetails(eventId: UInt) {
        viewModelScope.launch {
            _detailsUiState.value = _detailsUiState.value.copy(isLoading = true)
            eventHttpClient
                .getEventDetails(eventId)
                .onSuccess { event ->
                    _detailsUiState.value =
                        _detailsUiState.value.copy(
                            event = event,
                            isLoading = false,
                            error = null,
                        )
                }.onFailure {
                    _detailsUiState.value =
                        _detailsUiState.value.copy(
                            isLoading = false,
                            error = "Failed to load event details",
                        )
                }
        }
    }

    fun joinEvent(eventId: UInt) {
        viewModelScope.launch {
            _detailsUiState.value = _detailsUiState.value.copy(isJoining = true)
            eventHttpClient
                .joinEvent(eventId)
                .onSuccess { updatedEvent ->
                    _detailsUiState.value =
                        _detailsUiState.value.copy(
                            isJoining = false,
                            error = null,
                            event = updatedEvent,
                        )
                    _events.send(EventScreenEvent.ShowSnackbar("Joined event successfully"))
                }.onFailure {
                    _detailsUiState.value =
                        _detailsUiState.value.copy(
                            isJoining = false,
                            error = "Failed to join event",
                        )
                }
        }
    }

    fun updateEvent(
        eventId: UInt,
        request: UpdateEventRequest,
    ) {
        viewModelScope.launch {
            _detailsUiState.value = _detailsUiState.value.copy(isEditing = true)
            eventHttpClient
                .updateEventDetails(eventId, request)
                .onSuccess {
                    _detailsUiState.value =
                        _detailsUiState.value.copy(isEditing = false, error = null)
                    _events.send(EventScreenEvent.ShowSnackbar("Event updated successfully"))
                    _events.send(EventScreenEvent.NavigateBack)
                }.onFailure {
                    _detailsUiState.value =
                        _detailsUiState.value.copy(
                            isEditing = false,
                            error = "Failed to update event",
                        )
                }
        }
    }

    fun leaveEvent(eventId: UInt) {
        viewModelScope.launch {
            _detailsUiState.value = _detailsUiState.value.copy(isLeaving = true)
            eventHttpClient
                .leaveEvent(eventId)
                .onSuccess {
                    loadEventDetails(eventId)
                    _detailsUiState.value =
                        _detailsUiState.value.copy(isEditing = false, error = null)
                    _events.send(EventScreenEvent.ShowSnackbar("Left event successfully"))
                }.onFailure {
                    _detailsUiState.value =
                        _detailsUiState.value.copy(
                            isLeaving = false,
                            error = "Failed to leave event",
                        )
                }
        }
    }
}
