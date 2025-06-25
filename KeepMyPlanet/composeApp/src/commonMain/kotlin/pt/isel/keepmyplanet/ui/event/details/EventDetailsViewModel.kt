package pt.isel.keepmyplanet.ui.event.details

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import pt.isel.keepmyplanet.data.api.EventApi
import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.domain.event.Event
import pt.isel.keepmyplanet.domain.event.EventStatus
import pt.isel.keepmyplanet.domain.user.UserInfo
import pt.isel.keepmyplanet.dto.event.ChangeEventStatusRequest
import pt.isel.keepmyplanet.mapper.event.toEvent
import pt.isel.keepmyplanet.mapper.user.toUserInfo
import pt.isel.keepmyplanet.ui.event.details.states.EventDetailsEvent
import pt.isel.keepmyplanet.ui.event.details.states.EventDetailsUiState
import pt.isel.keepmyplanet.ui.viewmodel.BaseViewModel

class EventDetailsViewModel(
    private val eventApi: EventApi,
    private val currentUser: UserInfo,
) : BaseViewModel<EventDetailsUiState>(EventDetailsUiState()) {
    override fun handleErrorWithMessage(message: String) {
        sendEvent(EventDetailsEvent.ShowSnackbar(message))
    }

    fun loadEventDetails(eventId: Id) {
        viewModelScope.launch {
            setState { copy(isLoading = true, error = null) }
            try {
                coroutineScope {
                    val detailsDeferred = async { eventApi.getEventDetails(eventId.value) }
                    val participantsDeferred =
                        async { eventApi.getEventParticipants(eventId.value) }
                    val details = detailsDeferred.await().getOrThrow()
                    val participants = participantsDeferred.await().getOrThrow()
                    val event = details.toEvent()
                    setState {
                        copy(
                            event = event,
                            isCurrentUserOrganizer = event.organizerId == currentUser.id,
                            isCurrentUserParticipant =
                                event.participantsIds.contains(currentUser.id),
                            participants = participants.map { it.toUserInfo() },
                        )
                    }
                }
            } catch (error: Throwable) {
                val message = getErrorMessage("Failed to load event details", error)
                setState { copy(error = message) }
            } finally {
                setState { copy(isLoading = false) }
            }
        }
    }

    private fun performAction(
        actionState: EventDetailsUiState.ActionState,
        successMessage: String,
        errorMessagePrefix: String,
        apiCall: suspend (UInt) -> Result<Event>,
    ) {
        val eventId = getEventId() ?: return
        launchWithResult(
            onStart = { copy(actionState = actionState) },
            onFinally = { copy(actionState = EventDetailsUiState.ActionState.IDLE) },
            block = { apiCall(eventId.value) },
            onSuccess = { eventResponse ->
                updateEventInState(eventResponse)
                sendEvent(EventDetailsEvent.ShowSnackbar(successMessage))
                loadEventDetails(eventId)
            },
            onError = { handleErrorWithMessage(getErrorMessage(errorMessagePrefix, it)) },
        )
    }

    fun joinEvent() =
        performAction(
            EventDetailsUiState.ActionState.JOINING,
            "Joined event successfully",
            "Failed to join event",
        ) {
            eventApi.joinEvent(it).map { eventResponse -> eventResponse.toEvent() }
        }

    fun leaveEvent() =
        performAction(
            EventDetailsUiState.ActionState.LEAVING,
            "Left event successfully",
            "Failed to leave event",
        ) {
            eventApi.leaveEvent(it).map { eventResponse -> eventResponse.toEvent() }
        }

    fun changeEventStatus(newStatus: EventStatus) {
        val actionState =
            when (newStatus) {
                EventStatus.CANCELLED -> EventDetailsUiState.ActionState.CANCELLING
                EventStatus.COMPLETED -> EventDetailsUiState.ActionState.COMPLETING
                else -> EventDetailsUiState.ActionState.IDLE
            }
        val successMessage =
            when (newStatus) {
                EventStatus.CANCELLED -> "Event has been cancelled"
                EventStatus.COMPLETED -> "Event marked as complete"
                else -> "Event status updated"
            }
        val errorMessagePrefix =
            when (newStatus) {
                EventStatus.CANCELLED -> "Failed to cancel event"
                EventStatus.COMPLETED -> "Failed to complete event"
                else -> "Failed to update event status"
            }

        performAction(
            actionState = actionState,
            successMessage = successMessage,
            errorMessagePrefix = errorMessagePrefix,
            apiCall = { eventId ->
                val request = ChangeEventStatusRequest(newStatus)
                eventApi.changeEventStatus(eventId, request).map { it.toEvent() }
            },
        )
    }

/*    fun cancelEvent() =
        performAction(
            EventDetailsUiState.ActionState.CANCELLING,
            "Event has been cancelled",
            "Failed to cancel event",
        ) {
            eventApi.cancelEvent(it).map { eventResponse -> eventResponse.toEvent() }
        }

    fun completeEvent() =
        performAction(
            EventDetailsUiState.ActionState.COMPLETING,
            "Event marked as complete",
            "Failed to complete event",
        ) {
            eventApi.completeEvent(it).map { eventResponse -> eventResponse.toEvent() }
        }*/

    fun deleteEvent() {
        val eventId = getEventId() ?: return
        launchWithResult(
            onStart = { copy(actionState = EventDetailsUiState.ActionState.DELETING) },
            onFinally = { copy(actionState = EventDetailsUiState.ActionState.IDLE) },
            block = { eventApi.deleteEvent(eventId.value) },
            onSuccess = {
                sendEvent(EventDetailsEvent.ShowSnackbar("Event deleted successfully"))
                sendEvent(EventDetailsEvent.EventDeleted)
            },
            onError = { handleErrorWithMessage(getErrorMessage("Failed to delete event", it)) },
        )
    }

    fun onQrCodeIconClicked() {
        val state = currentState
        if (!state.canUseQrFeature()) return
        val eventId = state.event?.id ?: return

        if (state.isCurrentUserOrganizer) {
            sendEvent(EventDetailsEvent.NavigateToManageAttendance(eventId))
        } else {
            sendEvent(EventDetailsEvent.NavigateToMyQrCode(currentUser.id))
        }
    }

    private fun getEventId(): Id? = currentState.event?.id

    private fun updateEventInState(updatedEvent: Event) {
        setState {
            copy(
                event = updatedEvent,
                isCurrentUserOrganizer = updatedEvent.organizerId == currentUser.id,
                isCurrentUserParticipant = updatedEvent.participantsIds.contains(currentUser.id),
            )
        }
    }
}
