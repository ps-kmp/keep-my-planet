package pt.isel.keepmyplanet.ui.event.details

import pt.isel.keepmyplanet.data.repository.DefaultEventRepository
import pt.isel.keepmyplanet.data.repository.DefaultZoneRepository
import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.domain.event.Event
import pt.isel.keepmyplanet.domain.event.EventStatus
import pt.isel.keepmyplanet.domain.user.UserInfo
import pt.isel.keepmyplanet.dto.event.ChangeEventStatusRequest
import pt.isel.keepmyplanet.session.SessionManager
import pt.isel.keepmyplanet.ui.base.BaseViewModel
import pt.isel.keepmyplanet.ui.event.details.states.EventDetailsEvent
import pt.isel.keepmyplanet.ui.event.details.states.EventDetailsUiState

class EventDetailsViewModel(
    private val eventRepository: DefaultEventRepository,
    private val zoneRepository: DefaultZoneRepository,
    private val sessionManager: SessionManager,
) : BaseViewModel<EventDetailsUiState>(EventDetailsUiState()) {
    private val currentUser: UserInfo
        get() =
            sessionManager.userSession.value?.userInfo
                ?: throw IllegalStateException("EventDetailsViewModel requires a logged-in user.")

    override fun handleErrorWithMessage(message: String) {
        sendEvent(EventDetailsEvent.ShowSnackbar(message))
    }

    fun loadEventDetails(eventId: Id) {
        launchWithResult(
            onStart = { copy(isLoading = true, error = null) },
            onFinally = { copy(isLoading = false) },
            block = { eventRepository.getEventDetailsBundle(eventId) },
            onSuccess = { bundle ->
                setState {
                    copy(
                        event = bundle.event,
                        isCurrentUserOrganizer = bundle.event.organizerId == currentUser.id,
                        isCurrentUserParticipant =
                            bundle.event.participantsIds.contains(currentUser.id),
                        participants = bundle.participants,
                        error = null,
                    )
                }
            },
            onError = { error ->
                val message = getErrorMessage("Failed to load event details", error)
                setState { copy(error = message) }
            },
        )
    }

    private fun performAction(
        actionState: EventDetailsUiState.ActionState,
        successMessage: String,
        errorMessagePrefix: String,
        apiCall: suspend (Id) -> Result<Event>,
    ) {
        val eventId = getEventId() ?: return
        launchWithResult(
            onStart = { copy(actionState = actionState) },
            onFinally = { copy(actionState = EventDetailsUiState.ActionState.IDLE) },
            block = { apiCall(eventId) },
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
            eventRepository.joinEvent(it)
        }

    fun leaveEvent() =
        performAction(
            EventDetailsUiState.ActionState.LEAVING,
            "Left event successfully",
            "Failed to leave event",
        ) {
            eventRepository.leaveEvent(it)
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
                eventRepository.changeEventStatus(eventId, request)
            },
        )
    }

    fun deleteEvent() {
        val eventId = getEventId() ?: return
        launchWithResult(
            onStart = { copy(actionState = EventDetailsUiState.ActionState.DELETING) },
            onFinally = { copy(actionState = EventDetailsUiState.ActionState.IDLE) },
            block = { eventRepository.deleteEvent(eventId) },
            onSuccess = {
                sendEvent(EventDetailsEvent.ShowSnackbar("Event deleted successfully"))
                sendEvent(EventDetailsEvent.EventDeleted)
            },
            onError = { handleErrorWithMessage(getErrorMessage("Failed to delete event", it)) },
        )
    }

    fun confirmZoneCleanliness(wasCleaned: Boolean) {
        val event = currentState.event ?: return

        if(wasCleaned){
            launchWithResult(
                onStart = { copy(actionState = EventDetailsUiState.ActionState.COMPLETING) },
                onFinally = { copy(actionState = EventDetailsUiState.ActionState.IDLE) },
                block = {
                    zoneRepository.confirmCleanliness(
                        zoneId = event.zoneId,
                        eventId = event.id,
                        wasCleaned = true
                    )
                },
                onSuccess = {
                    sendEvent(EventDetailsEvent.ShowSnackbar("Zone status confirmed successfully!"))
                    sendEvent(EventDetailsEvent.NavigateBack)
                },
                onError = { handleErrorWithMessage(getErrorMessage("Failed to confirm zone status", it)) }
            )
        } else {
            sendEvent(EventDetailsEvent.NavigateToUpdateZone(event.zoneId))
        }
    }

    fun onQrCodeIconClicked() {
        val state = currentState
        if (!state.canUseQrFeature) return
        val event = state.event ?: return

        if (state.isCurrentUserOrganizer) {
            sendEvent(EventDetailsEvent.NavigateToManageAttendance(event.id))
        } else {
            val organizerInfo = state.participants.find { it.id == event.organizerId }
            val organizerName = organizerInfo?.name?.value

            if (organizerName != null) {
                sendEvent(EventDetailsEvent.NavigateToMyQrCode(currentUser.id, organizerName))
            } else {
                handleErrorWithMessage("Could not retrieve organizer's name.")
            }
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
