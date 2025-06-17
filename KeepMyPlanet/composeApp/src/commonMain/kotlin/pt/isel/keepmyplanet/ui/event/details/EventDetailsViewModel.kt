package pt.isel.keepmyplanet.ui.event.details

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
import pt.isel.keepmyplanet.data.http.ApiException
import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.domain.event.Event
import pt.isel.keepmyplanet.mapper.event.toEvent
import pt.isel.keepmyplanet.ui.event.details.model.EventDetailsUiState

class EventDetailsViewModel(
    private val eventApi: EventApi,
) : ViewModel() {
    private val _detailsUiState = MutableStateFlow(EventDetailsUiState())
    val detailsUiState: StateFlow<EventDetailsUiState> = _detailsUiState.asStateFlow()

    private val _events = Channel<EventDetailsScreenEvent>(Channel.BUFFERED)
    val events: Flow<EventDetailsScreenEvent> = _events.receiveAsFlow()

    fun loadEventDetails(eventId: Id) {
        viewModelScope.launch {
            _detailsUiState.update { it.copy(isLoading = true) }
            eventApi
                .getEventDetails(eventId.value)
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

    private fun getEventId(): Id? = _detailsUiState.value.event?.id

    fun joinEvent() {
        val eventId = getEventId() ?: return
        viewModelScope.launch {
            _detailsUiState.update { it.copy(isJoining = true) }
            eventApi
                .joinEvent(eventId.value)
                .onSuccess { updatedEventResponse ->
                    updateEventInState(updatedEventResponse.toEvent())
                    _events.send(EventDetailsScreenEvent.ShowSnackbar("Joined event successfully"))
                    _events.send(EventDetailsScreenEvent.EventActionSuccess)
                }.onFailure { error ->
                    handleError("Failed to join event", error)
                }
            _detailsUiState.update { it.copy(isJoining = false) }
        }
    }

    fun leaveEvent() {
        val eventId = getEventId() ?: return
        viewModelScope.launch {
            _detailsUiState.update { it.copy(isLeaving = true) }
            eventApi
                .leaveEvent(eventId.value)
                .onSuccess { updatedEventResponse ->
                    updateEventInState(updatedEventResponse.toEvent())
                    _events.send(EventDetailsScreenEvent.ShowSnackbar("Left event successfully"))
                    _events.send(EventDetailsScreenEvent.EventActionSuccess)
                }.onFailure { error ->
                    handleError("Failed to leave event", error)
                }
            _detailsUiState.update { it.copy(isLeaving = false) }
        }
    }

    fun cancelEvent() {
        val eventId = getEventId() ?: return
        viewModelScope.launch {
            _detailsUiState.update { it.copy(isCancelling = true) }
            eventApi
                .cancelEvent(eventId.value)
                .onSuccess { updatedEventResponse ->
                    updateEventInState(updatedEventResponse.toEvent())
                    _events.send(EventDetailsScreenEvent.ShowSnackbar("Event has been cancelled"))
                    _events.send(EventDetailsScreenEvent.EventActionSuccess)
                }.onFailure { error ->
                    handleError("Failed to cancel event", error)
                }
            _detailsUiState.update { it.copy(isCancelling = false) }
        }
    }

    fun completeEvent() {
        val eventId = getEventId() ?: return
        viewModelScope.launch {
            _detailsUiState.update { it.copy(isCompleting = true) }
            eventApi
                .completeEvent(eventId.value)
                .onSuccess { updatedEventResponse ->
                    updateEventInState(updatedEventResponse.toEvent())
                    _events.send(EventDetailsScreenEvent.ShowSnackbar("Event marked as complete"))
                    _events.send(EventDetailsScreenEvent.EventActionSuccess)
                }.onFailure { error ->
                    handleError("Failed to complete event", error)
                }
            _detailsUiState.update { it.copy(isCompleting = false) }
        }
    }

    fun deleteEvent() {
        val eventId = getEventId() ?: return
        viewModelScope.launch {
            _detailsUiState.update { it.copy(isDeleting = true) }
            eventApi
                .deleteEvent(eventId.value)
                .onSuccess {
                    _events.send(EventDetailsScreenEvent.ShowSnackbar("Event deleted successfully"))
                    _events.send(EventDetailsScreenEvent.EventDeleted)
                }.onFailure { error ->
                    handleError("Failed to delete event", error)
                }
            _detailsUiState.update { it.copy(isDeleting = false) }
        }
    }

    private fun updateEventInState(updatedEvent: Event) {
        _detailsUiState.update { it.copy(event = updatedEvent) }
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
        _events.send(EventDetailsScreenEvent.ShowSnackbar(message))
    }
}
