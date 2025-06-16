package pt.isel.keepmyplanet.ui.event

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
import pt.isel.keepmyplanet.dto.event.CreateEventRequest
import pt.isel.keepmyplanet.dto.event.UpdateEventRequest
import pt.isel.keepmyplanet.mapper.event.toEvent
import pt.isel.keepmyplanet.ui.event.model.EventDetailsUiState
import pt.isel.keepmyplanet.ui.event.model.EventFilterType
import pt.isel.keepmyplanet.ui.event.model.EventFormUiState
import pt.isel.keepmyplanet.ui.event.model.EventListUiState
import pt.isel.keepmyplanet.ui.event.model.EventScreenEvent

class EventViewModel(
    private val eventApi: EventApi,
) : ViewModel() {
    private val _listUiState = MutableStateFlow(EventListUiState())
    val listUiState: StateFlow<EventListUiState> = _listUiState.asStateFlow()

    private val _detailsUiState = MutableStateFlow(EventDetailsUiState())
    val detailsUiState: StateFlow<EventDetailsUiState> = _detailsUiState.asStateFlow()

    private val _formUiState = MutableStateFlow(EventFormUiState())
    val formUiState: StateFlow<EventFormUiState> = _formUiState.asStateFlow()

    private val _events = Channel<EventScreenEvent>(Channel.BUFFERED)
    val events: Flow<EventScreenEvent> = _events.receiveAsFlow()

    private var searchJob: Job? = null

    init {
        loadEvents(isRefresh = true)
    }

    private fun loadEvents(isRefresh: Boolean = false) {
        val currentState = _listUiState.value
        val offset = if (isRefresh) 0 else currentState.offset

        if (currentState.isLoading || currentState.isAddingMore) return

        viewModelScope.launch {
            _listUiState.update { it.copy(isLoading = true) }

            if (!isRefresh) {
                _listUiState.update { it.copy(isAddingMore = true) }
            }

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
                            events = (currentEvents + newEvents.map { r -> r.toListItem() }).distinctBy { item -> item.id },
                            offset = if (isRefresh) newEvents.size else it.offset + newEvents.size,
                            hasMorePages = newEvents.size == it.limit,
                        )
                    }
                }.onFailure { error ->
                    handleError("Failed to load events", error)
                }

            _listUiState.update { it.copy(isLoading = false, isAddingMore = false) }
        }
    }

    fun loadNextPage() {
        val currentState = _listUiState.value
        if (currentState.isLoading || currentState.isAddingMore || !currentState.hasMorePages) return
        loadEvents()
    }

    fun onSearchQueryChanged(query: String) {
        _listUiState.update { it.copy(query = query) }
        searchJob?.cancel()
        searchJob =
            viewModelScope.launch {
                delay(500)
                loadEvents(isRefresh = true)
            }
    }

    fun onFilterChanged(filterType: EventFilterType) {
        if (_listUiState.value.filter == filterType) return
        _listUiState.update { it.copy(filter = filterType, query = "") }
        loadEvents(isRefresh = true)
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
                    loadEvents(isRefresh = true)
                    _events.send(EventScreenEvent.EventCreated(response.id))
                }.onFailure { error ->
                    _formUiState.update { it.copy(isSubmitting = false) }
                    handleError("Failed to create event", error)
                }
        }
    }

    fun loadEventDetails(eventId: UInt) {
        viewModelScope.launch {
            _detailsUiState.update { it.copy(isLoading = true) }
            eventApi
                .getEventDetails(eventId)
                .onSuccess { event ->
                    _detailsUiState.update {
                        it.copy(event = event.toEvent(), isLoading = false)
                    }
                }.onFailure { error ->
                    _detailsUiState.update { it.copy(isLoading = false) }
                    handleError("Failed to load event details", error)
                }
        }
    }

    fun joinEvent(eventId: UInt) {
        viewModelScope.launch {
            _detailsUiState.update { it.copy(isJoining = true) }
            eventApi
                .joinEvent(eventId)
                .onSuccess { updatedEvent ->
                    _detailsUiState.update {
                        it.copy(isJoining = false, event = updatedEvent.toEvent())
                    }
                    _events.send(EventScreenEvent.ShowSnackbar("Joined event successfully"))
                }.onFailure { error ->
                    _detailsUiState.update { it.copy(isJoining = false) }
                    handleError("Failed to join event", error)
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
                    handleError("Failed to update event", error)
                }
        }
    }

    fun leaveEvent(eventId: UInt) {
        viewModelScope.launch {
            _detailsUiState.update { it.copy(isLeaving = true) }
            eventApi
                .leaveEvent(eventId)
                .onSuccess { updatedEventResponse ->
                    _detailsUiState.update {
                        it.copy(isLeaving = false, event = updatedEventResponse.toEvent())
                    }
                    _events.send(EventScreenEvent.ShowSnackbar("Left event successfully"))
                }.onFailure { error ->
                    _detailsUiState.update { it.copy(isLeaving = false) }
                    handleError("Failed to leave event", error)
                }
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
        _events.send(EventScreenEvent.ShowSnackbar(message))
    }
}
