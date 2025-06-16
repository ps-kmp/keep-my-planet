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
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import pt.isel.keepmyplanet.data.api.EventApi
import pt.isel.keepmyplanet.data.http.ApiException
import pt.isel.keepmyplanet.data.mapper.toListItem
import pt.isel.keepmyplanet.domain.event.Event
import pt.isel.keepmyplanet.dto.event.CreateEventRequest
import pt.isel.keepmyplanet.dto.event.UpdateEventRequest
import pt.isel.keepmyplanet.mapper.event.toEvent
import pt.isel.keepmyplanet.mapper.event.toResponse
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

    fun loadNextPage() {
        val currentState = _listUiState.value
        if (currentState.isLoading || currentState.isAddingMore || !currentState.hasMorePages) return
        loadEvents()
    }

    fun refreshEvents() {
        loadEvents(isRefresh = true)
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
        _formUiState.update { it.copy(title = title, titleError = null) }
    }

    fun onDescriptionChanged(description: String) {
        _formUiState.update { it.copy(description = description, descriptionError = null) }
    }

    fun onStartDateChanged(startDate: String) {
        _formUiState.update { it.copy(startDate = startDate, startDateError = null) }
    }

    fun onMaxParticipantsChanged(maxParticipants: String) {
        _formUiState.update {
            it.copy(maxParticipants = maxParticipants, maxParticipantsError = null)
        }
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
        val now = Clock.System.now()
        val defaultStartDateInstant =
            now.plus(1, DateTimeUnit.HOUR, TimeZone.currentSystemDefault())
        val defaultStartDate =
            defaultStartDateInstant.toLocalDateTime(TimeZone.currentSystemDefault())

        _formUiState.value =
            EventFormUiState(
                startDate = defaultStartDate.toString().substringBeforeLast(':'),
            )
    }

    fun prepareFormForZone(zoneId: UInt) {
        _formUiState.update { it.copy(zoneId = zoneId.toString()) }
    }

    fun createEvent() {
        if (!validateForm(isCreate = true)) return

        viewModelScope.launch {
            val formState = _formUiState.value
            val request =
                CreateEventRequest(
                    title = formState.title,
                    description = formState.description,
                    startDate = formState.startDate,
                    zoneId = formState.zoneId.toUInt(),
                    maxParticipants = formState.maxParticipants.toIntOrNull(),
                )

            _formUiState.update { it.copy(isSubmitting = true) }
            eventApi
                .createEvent(request)
                .onSuccess { response ->
                    loadEvents(isRefresh = true)
                    _events.send(EventScreenEvent.EventCreated(response.id))
                }.onFailure { error ->
                    handleError("Failed to create event", error)
                }
            _formUiState.update { it.copy(isSubmitting = false) }
        }
    }

    fun updateEvent(eventId: UInt) {
        if (!validateForm(isCreate = false)) return

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
                .onSuccess { updatedEventResponse ->
                    updateEventInStates(updatedEventResponse.toEvent())
                    _events.send(EventScreenEvent.ShowSnackbar("Event updated successfully"))
                    _events.send(EventScreenEvent.NavigateBack)
                    resetFormState()
                }.onFailure { error ->
                    handleError("Failed to update event", error)
                }
            _formUiState.update { it.copy(isSubmitting = false) }
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
                .onSuccess { updatedEventResponse ->
                    updateEventInStates(updatedEventResponse.toEvent())
                    _events.send(EventScreenEvent.ShowSnackbar("Joined event successfully"))
                }.onFailure { error ->
                    handleError("Failed to join event", error)
                }
            _detailsUiState.update { it.copy(isJoining = false) }
        }
    }

    fun leaveEvent(eventId: UInt) {
        viewModelScope.launch {
            _detailsUiState.update { it.copy(isLeaving = true) }
            eventApi
                .leaveEvent(eventId)
                .onSuccess { updatedEventResponse ->
                    updateEventInStates(updatedEventResponse.toEvent())
                    _events.send(EventScreenEvent.ShowSnackbar("Left event successfully"))
                }.onFailure { error ->
                    handleError("Failed to leave event", error)
                }
            _detailsUiState.update { it.copy(isLeaving = false) }
        }
    }

    private fun loadEvents(isRefresh: Boolean = false) {
        val currentState = _listUiState.value
        val offset = if (isRefresh) 0 else currentState.offset

        if (currentState.isLoading || currentState.isAddingMore) return

        viewModelScope.launch {
            _listUiState.update {
                it.copy(
                    isLoading = isRefresh,
                    isAddingMore = !isRefresh,
                    error = null,
                )
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
                    val message =
                        when (error) {
                            is ApiException -> error.error.message
                            else -> "Failed to load events: ${error.message ?: "Unknown error"}"
                        }
                    _listUiState.update { it.copy(error = message) }
                }

            _listUiState.update { it.copy(isLoading = false, isAddingMore = false) }
        }
    }

    fun cancelEvent(eventId: UInt) {
        viewModelScope.launch {
            _detailsUiState.update { it.copy(isCancelling = true) }
            eventApi
                .cancelEvent(eventId)
                .onSuccess { updatedEventResponse ->
                    updateEventInStates(updatedEventResponse.toEvent())
                    _events.send(EventScreenEvent.ShowSnackbar("Event has been cancelled"))
                }.onFailure { error ->
                    handleError("Failed to cancel event", error)
                }
            _detailsUiState.update { it.copy(isCancelling = false) }
        }
    }

    fun completeEvent(eventId: UInt) {
        viewModelScope.launch {
            _detailsUiState.update { it.copy(isCompleting = true) }
            eventApi
                .completeEvent(eventId)
                .onSuccess { updatedEventResponse ->
                    updateEventInStates(updatedEventResponse.toEvent())
                    _events.send(EventScreenEvent.ShowSnackbar("Event marked as complete"))
                }.onFailure { error ->
                    handleError("Failed to complete event", error)
                }
            _detailsUiState.update { it.copy(isCompleting = false) }
        }
    }

    fun deleteEvent(eventId: UInt) {
        viewModelScope.launch {
            _detailsUiState.update { it.copy(isDeleting = true) }
            eventApi
                .deleteEvent(eventId)
                .onSuccess {
                    _events.send(EventScreenEvent.ShowSnackbar("Event deleted successfully"))
                    _events.send(EventScreenEvent.EventDeleted)
                    loadEvents(isRefresh = true)
                }.onFailure { error ->
                    handleError("Failed to delete event", error)
                }
            _detailsUiState.update { it.copy(isDeleting = false) }
        }
    }

    private fun updateEventInStates(updatedEvent: Event) {
        if (_detailsUiState.value.event?.id == updatedEvent.id) {
            _detailsUiState.update { it.copy(event = updatedEvent) }
        }
        _listUiState.update { currentState ->
            val eventIndex = currentState.events.indexOfFirst { it.id == updatedEvent.id }
            if (eventIndex != -1) {
                val mutableEvents = currentState.events.toMutableList()
                mutableEvents[eventIndex] = updatedEvent.toResponse().toListItem()
                currentState.copy(events = mutableEvents)
            } else {
                currentState
            }
        }
    }

    private fun validateForm(isCreate: Boolean): Boolean {
        val formState = _formUiState.value
        val stateWithErrors =
            formState.copy(
                titleError = if (formState.title.isBlank()) "Title cannot be empty" else null,
                descriptionError = if (formState.description.isBlank()) "Description cannot be empty" else null,
                startDateError =
                    try {
                        LocalDateTime.parse(formState.startDate)
                        null
                    } catch (_: Exception) {
                        "Invalid date format (YYYY-MM-DDTHH:MM)"
                    },
                maxParticipantsError =
                    if (formState.maxParticipants.isNotEmpty()) {
                        formState.maxParticipants.toUIntOrNull()?.let {
                            if (it <= 0U) "Must be a positive number" else null
                        } ?: "Must be a valid number"
                    } else {
                        null
                    },
                zoneIdError = if (isCreate && formState.zoneId.toUIntOrNull() == null) "Invalid Zone ID" else null,
            )
        _formUiState.update { stateWithErrors }
        return !stateWithErrors.hasErrors
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
