package pt.isel.keepmyplanet.ui.event

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
import pt.isel.keepmyplanet.data.api.EventApi
import pt.isel.keepmyplanet.data.mapper.toListItem
import pt.isel.keepmyplanet.dto.event.CreateEventRequest
import pt.isel.keepmyplanet.dto.event.UpdateEventRequest
import pt.isel.keepmyplanet.mapper.event.toEvent
import pt.isel.keepmyplanet.ui.event.model.EventDetailsUiState
import pt.isel.keepmyplanet.ui.event.model.EventFormUiState
import pt.isel.keepmyplanet.ui.event.model.EventListUiState
import pt.isel.keepmyplanet.ui.event.model.EventScreenEvent
import pt.isel.keepmyplanet.ui.user.model.UserInfo

class EventViewModel(
    private val eventApi: EventApi,
    private val user: UserInfo,
) : ViewModel() {
    private val _listUiState = MutableStateFlow(EventListUiState())
    val listUiState: StateFlow<EventListUiState> = _listUiState.asStateFlow()

    private val _detailsUiState = MutableStateFlow(EventDetailsUiState())
    val detailsUiState: StateFlow<EventDetailsUiState> = _detailsUiState.asStateFlow()

    private val _formUiState = MutableStateFlow(EventFormUiState())
    val formUiState: StateFlow<EventFormUiState> = _formUiState.asStateFlow()

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
            eventApi
                .searchAllEvents(query, limit, offset)
                .onSuccess { events ->
                    _listUiState.update {
                        it.copy(
                            events = events.map { response -> response.toListItem() },
                            isLoading = false,
                            error = null,
                            limit = limit,
                            offset = offset,
                        )
                    }
                }.onFailure {
                    _listUiState.update {
                        it.copy(isLoading = false, error = "Failed to load events")
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

    fun onTitleChanged(title: String) {
        _formUiState.update { it.copy(title = title) }
    }

    fun onDescriptionChanged(description: String) {
        _formUiState.update { it.copy(description = description) }
    }

    fun onStartDateChanged(startDate: String) {
        _formUiState.update { it.copy(startDate = startDate) }
    }

    fun onMaxParticipantsChanged(maxParticipants: String) {
        _formUiState.update { it.copy(maxParticipants = maxParticipants) }
    }

    fun onZoneIdChanged(zoneId: String) {
        _formUiState.update { it.copy(zoneId = zoneId) }
    }

    fun prepareFormForEdit() {
        detailsUiState.value.event?.let { event ->
            _formUiState.value =
                EventFormUiState(
                    title = event.title.value,
                    description = event.description.value,
                    startDate = event.period.start.toString(),
                    maxParticipants = event.maxParticipants?.toString() ?: "",
                    zoneId = event.zoneId.value.toString(),
                )
        }
    }

    fun resetFormState() {
        _formUiState.value = EventFormUiState()
    }

    fun createEvent() {
        viewModelScope.launch {
            val formState = _formUiState.value
            val zoneId = formState.zoneId.toUIntOrNull()
            if (zoneId == null) {
                _events.send(EventScreenEvent.ShowSnackbar("Invalid Zone ID."))
                return@launch
            }

            val request =
                CreateEventRequest(
                    title = formState.title,
                    description = formState.description,
                    startDate = formState.startDate,
                    zoneId = zoneId,
                    maxParticipants = formState.maxParticipants.toIntOrNull(),
                )

            _formUiState.update { it.copy(isSubmitting = true) }
            eventApi
                .createEvent(request)
                .onSuccess { response ->
                    _formUiState.update { it.copy(isSubmitting = false) }
                    val newEvent = response.toListItem()
                    _listUiState.update {
                        it.copy(events = listOf(newEvent) + it.events, isLoading = false)
                    }
                    _events.send(EventScreenEvent.EventCreated(response.id))
                }.onFailure { error ->
                    _formUiState.update { it.copy(isSubmitting = false) }
                    _events.send(EventScreenEvent.ShowSnackbar("Failed to create event: ${error.message}"))
                }
        }
    }

    fun loadEventDetails(eventId: UInt) {
        viewModelScope.launch {
            _detailsUiState.value = _detailsUiState.value.copy(isLoading = true)
            eventApi
                .getEventDetails(eventId)
                .onSuccess { event ->
                    _detailsUiState.value =
                        _detailsUiState.value.copy(
                            event = event.toEvent(),
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
            eventApi
                .joinEvent(eventId)
                .onSuccess { updatedEvent ->
                    _detailsUiState.value =
                        _detailsUiState.value.copy(
                            isJoining = false,
                            error = null,
                            event = updatedEvent.toEvent(),
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

    fun updateEvent(eventId: UInt) {
        viewModelScope.launch {
            val formState = _formUiState.value
            val request =
                UpdateEventRequest(
                    title = formState.title,
                    description = formState.description,
                    startDate = formState.startDate,
                    maxParticipants = formState.maxParticipants.toIntOrNull(),
                )

            _formUiState.update { it.copy(isSubmitting = true) }
            eventApi
                .updateEventDetails(eventId, request)
                .onSuccess {
                    _formUiState.update { it.copy(isSubmitting = false) }
                    _events.send(EventScreenEvent.ShowSnackbar("Event updated successfully"))
                    _events.send(EventScreenEvent.NavigateBack)
                    resetFormState()
                }.onFailure { error ->
                    _formUiState.update { it.copy(isSubmitting = false) }
                    _events.send(EventScreenEvent.ShowSnackbar("Failed to update event: ${error.message}"))
                }
        }
    }

    fun leaveEvent(eventId: UInt) {
        viewModelScope.launch {
            _detailsUiState.value = _detailsUiState.value.copy(isLeaving = true)
            eventApi
                .leaveEvent(eventId)
                .onSuccess { updatedEventResponse ->
                    _detailsUiState.update {
                        it.copy(isLeaving = false, event = updatedEventResponse.toEvent())
                    }
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
