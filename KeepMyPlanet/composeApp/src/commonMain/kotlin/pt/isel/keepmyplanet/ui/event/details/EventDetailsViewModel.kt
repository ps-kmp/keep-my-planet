package pt.isel.keepmyplanet.ui.event.details

import pt.isel.keepmyplanet.data.repository.EventApiRepository
import pt.isel.keepmyplanet.data.repository.ZoneApiRepository
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
    private val eventRepository: EventApiRepository,
    private val zoneRepository: ZoneApiRepository,
    private val sessionManager: SessionManager,
) : BaseViewModel<EventDetailsUiState>(
        EventDetailsUiState(currentUser = sessionManager.userSession.value?.userInfo),
    ) {
    private val currentUser: UserInfo? get() = sessionManager.userSession.value?.userInfo

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
                    copy(event = bundle.event, participants = bundle.participants, error = null)
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
        val eventId = getEventId() ?: return

        val actionState =
            when (newStatus) {
                EventStatus.CANCELLED -> EventDetailsUiState.ActionState.CANCELLING
                EventStatus.COMPLETED -> EventDetailsUiState.ActionState.COMPLETING
                else -> return
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

        launchWithResult(
            onStart = { copy(actionState = actionState) },
            onFinally = { copy(actionState = EventDetailsUiState.ActionState.IDLE) },
            block = {
                val request = ChangeEventStatusRequest(newStatus)
                eventRepository.changeEventStatus(eventId, request)
            },
            onSuccess = { eventResponse ->
                updateEventInState(eventResponse)
                loadEventDetails(eventId)
            },
            onError = { handleErrorWithMessage(getErrorMessage(errorMessagePrefix, it)) },
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

        if (wasCleaned) {
            launchWithResult(
                onStart = { copy(actionState = EventDetailsUiState.ActionState.COMPLETING) },
                onFinally = { copy(actionState = EventDetailsUiState.ActionState.IDLE) },
                block = {
                    zoneRepository.confirmCleanliness(
                        zoneId = event.zoneId,
                        eventId = event.id,
                        wasCleaned = true,
                    )
                },
                onSuccess = { _ ->
                    sendEvent(
                        EventDetailsEvent.ShowSnackbar("Zone status confirmed successfully!"),
                    )
                    sendEvent(EventDetailsEvent.NavigateBack)
                },
                onError = {
                    handleErrorWithMessage(
                        getErrorMessage("Failed to confirm zone status", it),
                    )
                },
            )
        } else {
            sendEvent(EventDetailsEvent.NavigateToUpdateZone(event.zoneId))
        }
    }

    fun onQrCodeIconClicked() {
        val state = currentState
        if (!state.canUseQrFeature) return
        val event = state.event ?: return
        val user = currentUser ?: return

        if (state.isCurrentUserOrganizer) {
            sendEvent(EventDetailsEvent.NavigateToManageAttendance(event.id))
        } else {
            val organizerInfo = state.participants.find { it.id == event.organizerId }
            val organizerName = organizerInfo?.name?.value

            if (organizerName != null) {
                sendEvent(EventDetailsEvent.NavigateToMyQrCode(user.id, organizerName))
            } else {
                handleErrorWithMessage("Could not retrieve organizer's name.")
            }
        }
    }

    private fun getEventId(): Id? = currentState.event?.id

    private fun updateEventInState(updatedEvent: Event) {
        setState { copy(event = updatedEvent) }
    }

    fun initiateTransfer(nomineeId: Id) {
        val eventId = getEventId() ?: return
        launchWithResult(
            onStart = { copy(actionState = EventDetailsUiState.ActionState.INITIATING_TRANSFER) },
            onFinally = { copy(actionState = EventDetailsUiState.ActionState.IDLE) },
            block = { eventRepository.initiateTransfer(eventId, nomineeId) },
            onSuccess = { updatedEvent ->
                updateEventInState(updatedEvent)
                sendEvent(EventDetailsEvent.ShowSnackbar("Transfer request sent successfully."))
            },
            onError = {
                handleErrorWithMessage(
                    getErrorMessage("Failed to initiate transfer", it),
                )
            },
        )
    }

    fun respondToTransfer(accepted: Boolean) {
        val eventId = getEventId() ?: return
        launchWithResult(
            onStart = {
                copy(
                    actionState = EventDetailsUiState.ActionState.RESPONDING_TO_TRANSFER,
                )
            },
            onFinally = { copy(actionState = EventDetailsUiState.ActionState.IDLE) },
            block = { eventRepository.respondToTransfer(eventId, accepted) },
            onSuccess = { updatedEvent ->
                updateEventInState(updatedEvent)
                val message =
                    if (accepted) "You are now the new organizer!" else "Transfer request declined."
                sendEvent(EventDetailsEvent.ShowSnackbar(message))
                loadEventDetails(eventId)
            },
            onError = {
                handleErrorWithMessage(
                    getErrorMessage("Failed to respond to transfer", it),
                )
            },
        )
    }

    fun onTransferOwnershipClicked() {
        if (currentState.canTransferOwnership) {
            sendEvent(EventDetailsEvent.ShowParticipantSelectionDialog)
        }
    }

    fun onShowNotificationDialog() {
        setState {
            copy(
                showNotificationDialog = true,
                notificationTitle = "Update about '${event?.title?.value ?: "the event"}'",
            )
        }
    }

    fun onDismissNotificationDialog() {
        setState {
            copy(
                showNotificationDialog = false,
                notificationTitle = "",
                notificationMessage = "",
                notificationError = null,
            )
        }
    }

    fun onNotificationTitleChanged(title: String) {
        setState { copy(notificationTitle = title, notificationError = null) }
    }

    fun onNotificationMessageChanged(message: String) {
        setState { copy(notificationMessage = message, notificationError = null) }
    }

    fun sendManualNotification() {
        val eventId = getEventId() ?: return
        if (!currentState.isSendNotificationButtonEnabled) return

        launchWithResult<Unit>(
            onStart = { copy(actionState = EventDetailsUiState.ActionState.SENDING_NOTIFICATION) },
            onFinally = {
                copy(
                    actionState = EventDetailsUiState.ActionState.IDLE,
                    showNotificationDialog = false,
                    notificationTitle = "",
                    notificationMessage = "",
                    notificationError = null,
                )
            },
            block = {
                eventRepository.sendManualNotification(
                    eventId,
                    currentState.notificationTitle,
                    currentState.notificationMessage,
                )
            },
            onSuccess = {
                sendEvent(
                    EventDetailsEvent.ShowSnackbar("Notification sent to all participants."),
                )
            },
            onError = {
                handleErrorWithMessage(
                    getErrorMessage("Failed to send notification", it),
                )
            },
        )
    }
}
