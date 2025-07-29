package pt.isel.keepmyplanet.ui.event.details

import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
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
        viewModelScope.launch {
            setState { copy(isLoading = true, error = null, isLoadingParticipants = true) }
            eventRepository.getEventDetailsBundle(eventId).collectLatest { result ->
                result
                    .onSuccess { bundle ->
                        val zoneResult =
                            zoneRepository.getZoneDetails(
                                bundle.event.zoneId,
                                forceNetwork = true,
                            )
                        setState {
                            copy(
                                event = bundle.event,
                                participants = bundle.participants,
                                zone = zoneResult.getOrNull(),
                                isLoading = false,
                                isLoadingParticipants = false,
                            )
                        }
                    }.onFailure { error ->
                        val message = getErrorMessage("Failed to load event details", error)
                        if (currentState.event == null) {
                            setState {
                                copy(
                                    error = message,
                                    isLoading = false,
                                    isLoadingParticipants = false,
                                )
                            }
                        } else {
                            handleErrorWithMessage(message)
                            setState { copy(isLoading = false, isLoadingParticipants = false) }
                        }
                    }
            }
        }
    }

    fun joinEvent() {
        val user = currentUser ?: return
        val originalEvent = currentState.event ?: return
        val originalParticipants = currentState.participants

        val optimisticEvent =
            originalEvent.copy(
                participantsIds =
                    originalEvent.participantsIds + user.id,
            )
        val optimisticParticipants = (originalParticipants + user).distinctBy { it.id }
        setState {
            copy(
                actionState = EventDetailsUiState.ActionState.JOINING,
                event = optimisticEvent,
                participants = optimisticParticipants,
            )
        }

        launchWithResult(
            block = { eventRepository.joinEvent(originalEvent.id) },
            onSuccess = { updatedEvent ->
                setState {
                    copy(
                        event = updatedEvent,
                        participants = participants + user,
                    )
                }
                sendEvent(EventDetailsEvent.ShowSnackbar("Successfully joined event!"))
            },
            onError = {
                // 3b. On Failure, revert optimistic change
                setState { copy(event = originalEvent, participants = originalParticipants) }
                handleErrorWithMessage(getErrorMessage("Failed to join event", it))
            },
            onFinally = { copy(actionState = EventDetailsUiState.ActionState.IDLE) },
        )
    }

    fun leaveEvent() {
        val user = currentUser ?: return
        val originalEvent = currentState.event ?: return
        val originalParticipants = currentState.participants

        val optimisticEvent =
            originalEvent.copy(
                participantsIds =
                    originalEvent.participantsIds - user.id,
            )
        val optimisticParticipants = originalParticipants.filter { it.id != user.id }
        setState {
            copy(
                actionState = EventDetailsUiState.ActionState.LEAVING,
                event = optimisticEvent,
                participants = optimisticParticipants,
            )
        }

        launchWithResult(
            block = { eventRepository.leaveEvent(originalEvent.id) },
            onSuccess = { updatedEvent ->
                setState {
                    copy(
                        event = updatedEvent,
                        participants = participants.filter { it.id != user.id },
                    )
                }
                sendEvent(EventDetailsEvent.ShowSnackbar("You have left the event."))
            },
            onError = {
                setState { copy(event = originalEvent, participants = originalParticipants) }
                handleErrorWithMessage(getErrorMessage("Failed to leave event", it))
            },
            onFinally = { copy(actionState = EventDetailsUiState.ActionState.IDLE) },
        )
    }

    fun changeEventStatus(newStatus: EventStatus) {
        val eventId = getEventId() ?: return
        val originalEvent = currentState.event ?: return

        val actionState =
            when (newStatus) {
                EventStatus.CANCELLED -> EventDetailsUiState.ActionState.CANCELLING
                EventStatus.COMPLETED -> EventDetailsUiState.ActionState.COMPLETING
                else -> return
            }

        val optimisticEvent = originalEvent.copy(status = newStatus)
        setState { copy(actionState = actionState, event = optimisticEvent) }

        val errorMessagePrefix =
            if (newStatus ==
                EventStatus.CANCELLED
            ) {
                "Failed to cancel event"
            } else {
                "Failed to complete event"
            }

        launchWithResult(
            block = {
                eventRepository.changeEventStatus(
                    eventId,
                    ChangeEventStatusRequest(newStatus),
                )
            },
            onSuccess = { updatedEvent ->
                updateEventInState(updatedEvent)
                sendEvent(
                    EventDetailsEvent.ShowSnackbar("Event status updated to ${newStatus.name}."),
                )
            },
            onError = {
                setState { copy(event = originalEvent) }
                handleErrorWithMessage(getErrorMessage(errorMessagePrefix, it))
            },
            onFinally = { copy(actionState = EventDetailsUiState.ActionState.IDLE) },
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

        launchWithResult(
            onStart = { copy(actionState = EventDetailsUiState.ActionState.COMPLETING) },
            onFinally = { copy(actionState = EventDetailsUiState.ActionState.IDLE) },
            block = {
                zoneRepository.confirmCleanliness(
                    zoneId = event.zoneId,
                    eventId = event.id,
                    wasCleaned = wasCleaned,
                )
            },
            onSuccess = { _ ->
                val message =
                    if (wasCleaned) {
                        "Zone status confirmed successfully!"
                    } else {
                        "Zone marked as not cleaned. You can now update its details."
                    }
                sendEvent(EventDetailsEvent.ShowSnackbar(message))
                eventRepository.invalidateEventCache(event.id)
                loadEventDetails(event.id)
                if (!wasCleaned) {
                    sendEvent(EventDetailsEvent.NavigateToUpdateZone(event.zoneId))
                }
            },
            onError = {
                handleErrorWithMessage(getErrorMessage("Failed to confirm zone status", it))
            },
        )
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
                handleErrorWithMessage(getErrorMessage("Failed to initiate transfer", it))
            },
        )
    }

    fun respondToTransfer(accepted: Boolean) {
        val eventId = getEventId() ?: return
        val originalEvent = currentState.event ?: return

        val optimisticEvent =
            originalEvent.copy(
                pendingOrganizerId = null,
                transferRequestTime = null,
            )
        setState {
            copy(
                actionState = EventDetailsUiState.ActionState.RESPONDING_TO_TRANSFER,
                event = optimisticEvent,
            )
        }

        launchWithResult(
            block = { eventRepository.respondToTransfer(eventId, accepted) },
            onSuccess = { updatedEvent ->
                updateEventInState(updatedEvent)
                val message =
                    if (accepted) {
                        "You are now the new organizer!"
                    } else {
                        "Transfer request declined."
                    }
                sendEvent(EventDetailsEvent.ShowSnackbar(message))
            },
            onError = {
                setState { copy(event = originalEvent) }
                handleErrorWithMessage(getErrorMessage("Failed to respond to transfer", it))
            },
            onFinally = { copy(actionState = EventDetailsUiState.ActionState.IDLE) },
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
                notificationTitle =
                    "Update about '${currentState.event?.title?.value ?: "the event"}'",
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
        val title = currentState.notificationTitle.trim()
        val message = currentState.notificationMessage.trim()

        if (title.isBlank() || message.isBlank()) {
            setState { copy(notificationError = "Title and message cannot be empty.") }
            return
        }

        launchWithResult(
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
