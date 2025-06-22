package pt.isel.keepmyplanet.ui.event.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pt.isel.keepmyplanet.data.api.EventApi
import pt.isel.keepmyplanet.data.http.ApiException
import pt.isel.keepmyplanet.data.mapper.toUserInfo
import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.domain.event.Event
import pt.isel.keepmyplanet.mapper.event.toEvent
import pt.isel.keepmyplanet.ui.event.details.model.EventDetailsEvent
import pt.isel.keepmyplanet.ui.event.details.model.EventDetailsUiState
import pt.isel.keepmyplanet.ui.user.profile.model.UserInfo

class EventDetailsViewModel(
    private val eventApi: EventApi,
    private val currentUser: UserInfo,
) : ViewModel() {
    private val _uiState = MutableStateFlow(EventDetailsUiState())
    val uiState: StateFlow<EventDetailsUiState> = _uiState.asStateFlow()

    private val _events = Channel<EventDetailsEvent>(Channel.BUFFERED)
    val events: Flow<EventDetailsEvent> = _events.receiveAsFlow()

    fun loadEventDetails(eventId: Id) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                coroutineScope {
                    val detailsDeferred = async { eventApi.getEventDetails(eventId.value) }
                    val participantsDeferred =
                        async { eventApi.getEventParticipants(eventId.value) }

                    val detailsResult = detailsDeferred.await()
                    val participantsResult = participantsDeferred.await()

                    detailsResult
                        .onSuccess { eventResponse ->
                            val event = eventResponse.toEvent()
                            _uiState.update {
                                it.copy(
                                    event = event,
                                    isCurrentUserOrganizer = event.organizerId == currentUser.id,
                                    isCurrentUserParticipant =
                                        event.participantsIds.contains(currentUser.id),
                                )
                            }
                        }.onFailure { throw it }

                    participantsResult
                        .onSuccess { participantsResponse ->
                            _uiState.update {
                                it.copy(
                                    participants = participantsResponse.map { p -> p.toUserInfo() },
                                )
                            }
                        }.onFailure { throw it }
                }
            } catch (error: Throwable) {
                val message = getErrorMessage("Failed to load event details", error)
                _uiState.update { it.copy(error = message) }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun joinEvent() {
        val eventId = getEventId() ?: return
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    actionState = EventDetailsUiState.ActionState.JOINING,
                )
            }
            eventApi
                .joinEvent(eventId.value)
                .onSuccess {
                    _events.send(EventDetailsEvent.ShowSnackbar("Joined event successfully"))
                    loadEventDetails(eventId)
                }.onFailure { error ->
                    handleError("Failed to join event", error)
                }
            _uiState.update { it.copy(actionState = EventDetailsUiState.ActionState.IDLE) }
        }
    }

    fun leaveEvent() {
        val eventId = getEventId() ?: return
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    actionState = EventDetailsUiState.ActionState.LEAVING,
                )
            }
            eventApi
                .leaveEvent(eventId.value)
                .onSuccess {
                    _events.send(EventDetailsEvent.ShowSnackbar("Left event successfully"))
                    loadEventDetails(eventId)
                }.onFailure { error ->
                    handleError("Failed to leave event", error)
                }
            _uiState.update { it.copy(actionState = EventDetailsUiState.ActionState.IDLE) }
        }
    }

    fun cancelEvent() {
        val eventId = getEventId() ?: return
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    actionState = EventDetailsUiState.ActionState.CANCELLING,
                )
            }
            eventApi
                .cancelEvent(eventId.value)
                .onSuccess { updatedEventResponse ->
                    updateEventInState(updatedEventResponse.toEvent())
                    _events.send(EventDetailsEvent.ShowSnackbar("Event has been cancelled"))
                }.onFailure { error ->
                    handleError("Failed to cancel event", error)
                }
            _uiState.update { it.copy(actionState = EventDetailsUiState.ActionState.IDLE) }
        }
    }

    fun completeEvent() {
        val eventId = getEventId() ?: return
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    actionState = EventDetailsUiState.ActionState.COMPLETING,
                )
            }
            eventApi
                .completeEvent(eventId.value)
                .onSuccess { updatedEventResponse ->
                    updateEventInState(updatedEventResponse.toEvent())
                    _events.send(EventDetailsEvent.ShowSnackbar("Event marked as complete"))
                }.onFailure { error ->
                    handleError("Failed to complete event", error)
                }
            _uiState.update { it.copy(actionState = EventDetailsUiState.ActionState.IDLE) }
        }
    }

    fun deleteEvent() {
        val eventId = getEventId() ?: return
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    actionState = EventDetailsUiState.ActionState.DELETING,
                )
            }
            eventApi
                .deleteEvent(eventId.value)
                .onSuccess {
                    _events.send(EventDetailsEvent.ShowSnackbar("Event deleted successfully"))
                    _events.send(EventDetailsEvent.EventDeleted)
                }.onFailure { error ->
                    handleError("Failed to delete event", error)
                }
            _uiState.update { it.copy(actionState = EventDetailsUiState.ActionState.IDLE) }
        }
    }

    fun onManageAttendanceClicked() {
        val eventId = getEventId() ?: return
        viewModelScope.launch {
            _events.send(EventDetailsEvent.NavigateToManageAttendance(eventId))
        }
    }

    fun onQrCodeIconClicked() {
        val state = _uiState.value
        if (!state.canUseQrFeature()) {
            return
        }

        val eventId = state.event?.id ?: return

        viewModelScope.launch {
            if (state.isCurrentUserOrganizer) {
                _events.send(EventDetailsEvent.NavigateToManageAttendance(eventId))
            } else {
                _events.send(
                    EventDetailsEvent.NavigateToMyQrCode(currentUser.id),
                )
            }
        }
    }

    private fun getEventId(): Id? = _uiState.value.event?.id

    private fun updateEventInState(updatedEvent: Event) {
        _uiState.update {
            it.copy(
                event = updatedEvent,
                isCurrentUserOrganizer = updatedEvent.organizerId == currentUser.id,
                isCurrentUserParticipant = updatedEvent.participantsIds.contains(currentUser.id),
            )
        }
    }

    private fun getErrorMessage(
        prefix: String,
        error: Throwable,
    ): String =
        when (error) {
            is ApiException -> error.error.message
            else -> "$prefix: ${error.message ?: "Unknown error"}"
        }

    private suspend fun handleError(
        prefix: String,
        error: Throwable,
    ) {
        val message = getErrorMessage(prefix, error)
        _events.send(EventDetailsEvent.ShowSnackbar(message))
    }
}
