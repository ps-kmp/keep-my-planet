package pt.isel.keepmyplanet.ui.event.forms

import kotlinx.datetime.LocalDateTime
import pt.isel.keepmyplanet.data.repository.DefaultEventRepository
import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.dto.event.CreateEventRequest
import pt.isel.keepmyplanet.dto.event.UpdateEventRequest
import pt.isel.keepmyplanet.ui.base.BaseViewModel
import pt.isel.keepmyplanet.ui.event.forms.states.EventFormEvent
import pt.isel.keepmyplanet.ui.event.forms.states.EventFormUiState

class EventFormViewModel(
    private val eventRepository: DefaultEventRepository,
) : BaseViewModel<EventFormUiState>(EventFormUiState()) {
    override fun handleErrorWithMessage(message: String) {
        sendEvent(EventFormEvent.ShowSnackbar(message))
    }

    fun prepareForCreate(zoneId: Id?) {
        setState { EventFormUiState(zoneId = zoneId?.value?.toString() ?: "", isEditMode = false) }
    }

    fun prepareForEdit(eventId: Id) {
        launchWithResult(
            onStart = { copy(isEditMode = true, isLoading = true, eventIdToEdit = eventId) },
            onFinally = { copy(isLoading = false) },
            block = { eventRepository.getEventDetails(eventId) },
            onSuccess = { event ->
                setState {
                    copy(
                        title = event.title.value,
                        description = event.description.value,
                        startDate = event.period.start.toString(),
                        maxParticipants = event.maxParticipants?.toString() ?: "",
                    )
                }
            },
            onError = {
                handleErrorWithMessage(getErrorMessage("Failed to load event for editing", it))
            },
        )
    }

    fun onTitleChanged(title: String) =
        setState {
            copy(title = title, titleError = null)
        }

    fun onDescriptionChanged(description: String) =
        setState {
            copy(description = description, descriptionError = null)
        }

    fun onStartDateChanged(startDate: String) =
        setState {
            copy(startDate = startDate, startDateError = null)
        }

    fun onMaxParticipantsChanged(maxParticipants: String) =
        setState {
            copy(maxParticipants = maxParticipants, maxParticipantsError = null)
        }

    fun onZoneIdChanged(zoneId: String) =
        setState {
            copy(zoneId = zoneId, zoneIdError = null)
        }

    fun submit() {
        if (currentState.isEditMode) updateEvent() else createEvent()
    }

    private fun createEvent() {
        if (!validateForm(isCreate = true)) return

        val request =
            CreateEventRequest(
                title = currentState.title,
                description = currentState.description,
                startDate = currentState.startDate,
                zoneId = currentState.zoneId.toUInt(),
                maxParticipants = currentState.maxParticipants.toIntOrNull(),
            )

        launchWithResult(
            onStart = { copy(actionState = EventFormUiState.ActionState.Submitting) },
            onFinally = { copy(actionState = EventFormUiState.ActionState.Idle) },
            block = { eventRepository.createEvent(request) },
            onSuccess = { response -> sendEvent(EventFormEvent.EventCreated(response.id)) },
            onError = { handleErrorWithMessage(getErrorMessage("Failed to create event", it)) },
        )
    }

    private fun updateEvent() {
        val eventId = currentState.eventIdToEdit ?: return
        if (!validateForm(isCreate = false)) return

        val request =
            UpdateEventRequest(
                title = currentState.title,
                description = currentState.description,
                startDate = currentState.startDate,
                maxParticipants = currentState.maxParticipants.toIntOrNull(),
            )

        launchWithResult(
            onStart = { copy(actionState = EventFormUiState.ActionState.Submitting) },
            onFinally = { copy(actionState = EventFormUiState.ActionState.Idle) },
            block = { eventRepository.updateEvent(eventId, request) },
            onSuccess = {
                sendEvent(EventFormEvent.ShowSnackbar("Event updated successfully"))
                sendEvent(EventFormEvent.NavigateBack)
            },
            onError = { handleErrorWithMessage(getErrorMessage("Failed to update event", it)) },
        )
    }

    private fun validateForm(isCreate: Boolean): Boolean {
        val stateWithErrors =
            currentState.copy(
                titleError = if (currentState.title.isBlank()) "Title cannot be empty" else null,
                descriptionError =
                    if (currentState.description.isBlank()) "Description cannot be empty" else null,
                startDateError =
                    try {
                        LocalDateTime.parse(currentState.startDate)
                        null
                    } catch (_: Exception) {
                        "Invalid date format (YYYY-MM-DDTHH:MM:SS)"
                    },
                maxParticipantsError =
                    if (currentState.maxParticipants.isNotEmpty()) {
                        val number = currentState.maxParticipants.toIntOrNull()
                        if (number == null) {
                            "Must be a valid number"
                        } else if (number <= 0) {
                            "Must be a positive number"
                        } else {
                            null
                        }
                    } else {
                        null
                    },
                zoneIdError =
                    if (isCreate && currentState.zoneId.toUIntOrNull() == null) {
                        "Invalid Zone ID"
                    } else {
                        null
                    },
            )
        setState { stateWithErrors }
        return !stateWithErrors.hasErrors
    }
}
