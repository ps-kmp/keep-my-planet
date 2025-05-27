package pt.isel.keepmyplanet.ui.screens.event

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDateTime
import pt.isel.keepmyplanet.data.model.EventInfo
import pt.isel.keepmyplanet.data.model.UserInfo
import pt.isel.keepmyplanet.data.service.EventService
import pt.isel.keepmyplanet.domain.common.Description
import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.domain.event.EventStatus
import pt.isel.keepmyplanet.domain.event.Period
import pt.isel.keepmyplanet.domain.event.Title
import pt.isel.keepmyplanet.dto.event.CreateEventRequest
import pt.isel.keepmyplanet.dto.event.UpdateEventRequest

class EventViewModel(
    private val eventService: EventService,
    private val user: UserInfo,
) : ViewModel() {
    private val _listUiState = MutableStateFlow(EventListUiState())
    val listUiState: StateFlow<EventListUiState> = _listUiState.asStateFlow()

    private val _detailsUiState = MutableStateFlow(EventDetailsUiState())
    val detailsUiState: StateFlow<EventDetailsUiState> = _detailsUiState.asStateFlow()

    private val _events = Channel<EventScreenEvent>(Channel.BUFFERED)
    val events: Flow<EventScreenEvent> = _events.receiveAsFlow()

/*    private val _lastCreatedEvent = MutableStateFlow<EventResponse?>(null)
    val lastCreatedEvent: StateFlow<EventResponse?> = _lastCreatedEvent.asStateFlow()*/

    init {
        loadEvents()
    }

    fun loadEvents(query: String? = null) {
        viewModelScope.launch {
            _listUiState.value = _listUiState.value.copy(isLoading = true)
            eventService
                .searchAllEvents(query)
                .onSuccess { events ->
                    _listUiState.value =
                        _listUiState.value.copy(
                            events =
                                events.map { response ->
                                    EventInfo(
                                        id = Id(response.id),
                                        title = Title(response.title),
                                        description = Description(response.description),
                                        period =
                                            Period(
                                                LocalDateTime.parse(response.startDate),
                                                LocalDateTime.parse(response.endDate),
                                            ),
                                        status = EventStatus.valueOf(response.status.uppercase()),
                                    )
                                },
                            isLoading = false,
                            error = null,
                        )
                }.onFailure {
                    _listUiState.value =
                        _listUiState.value.copy(
                            isLoading = false,
                            error = "Failed to load events",
                        )
                }
        }
    }

    fun createEvent(request: CreateEventRequest) {
        viewModelScope.launch {
            _listUiState.value = _listUiState.value.copy(isLoading = true)
            eventService
                .createEvent(request)
                .onSuccess { response ->
                    loadEvents()
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
            eventService
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
            eventService
                .joinEvent(eventId)
                .onSuccess {
                    loadEventDetails(eventId)
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
            eventService
                .updateEventDetails(eventId, request)
                .onSuccess {
                    loadEventDetails(eventId)
                    _events.send(EventScreenEvent.ShowSnackbar("Event updated successfully"))
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
            eventService
                .leaveEvent(eventId)
                .onSuccess {
                    loadEventDetails(eventId)
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
