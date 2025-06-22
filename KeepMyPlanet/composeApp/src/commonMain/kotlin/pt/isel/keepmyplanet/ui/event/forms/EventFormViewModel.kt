package pt.isel.keepmyplanet.ui.event.forms

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
import kotlinx.datetime.LocalDateTime
import pt.isel.keepmyplanet.data.api.EventApi
import pt.isel.keepmyplanet.data.http.ApiException
import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.dto.event.CreateEventRequest
import pt.isel.keepmyplanet.dto.event.UpdateEventRequest
import pt.isel.keepmyplanet.ui.event.forms.model.EventFormEvent
import pt.isel.keepmyplanet.ui.event.forms.model.EventFormUiState

class EventFormViewModel(
    private val eventApi: EventApi,
) : ViewModel() {
    private val _uiState = MutableStateFlow(EventFormUiState())
    val uiState: StateFlow<EventFormUiState> = _uiState.asStateFlow()

    private val _events = Channel<EventFormEvent>(Channel.BUFFERED)
    val events: Flow<EventFormEvent> = _events.receiveAsFlow()

    fun prepareForCreate(zoneId: Id?) {
        _uiState.value =
            EventFormUiState(
                zoneId = zoneId?.value?.toString() ?: "",
                isEditMode = false,
                eventIdToEdit = null,
            )
    }

    fun prepareForEdit(eventId: Id) {
        _uiState.update {
            it.copy(
                isEditMode = true,
                isLoading = true,
                eventIdToEdit = eventId,
            )
        }
        viewModelScope.launch {
            eventApi
                .getEventDetails(eventId.value)
                .onSuccess { event ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            title = event.title,
                            description = event.description,
                            startDate = event.startDate,
                            maxParticipants = event.maxParticipants?.toString() ?: "",
                        )
                    }
                }.onFailure { error ->
                    _uiState.update { it.copy(isLoading = false) }
                    handleError("Failed to load event for editing", error)
                }
        }
    }

    fun onTitleChanged(title: String) {
        _uiState.update { it.copy(title = title, titleError = null) }
    }

    fun onDescriptionChanged(description: String) {
        _uiState.update { it.copy(description = description, descriptionError = null) }
    }

    fun onStartDateChanged(startDate: String) {
        _uiState.update { it.copy(startDate = startDate, startDateError = null) }
    }

    fun onMaxParticipantsChanged(maxParticipants: String) {
        _uiState.update {
            it.copy(maxParticipants = maxParticipants, maxParticipantsError = null)
        }
    }

    fun onZoneIdChanged(zoneId: String) {
        _uiState.update { it.copy(zoneId = zoneId, zoneIdError = null) }
    }

    fun submit() {
        if (uiState.value.isEditMode) {
            updateEvent()
        } else {
            createEvent()
        }
    }

    private fun createEvent() {
        if (!validateForm(isCreate = true)) return

        viewModelScope.launch {
            val formState = _uiState.value
            val request =
                CreateEventRequest(
                    title = formState.title,
                    description = formState.description,
                    startDate = formState.startDate,
                    zoneId = formState.zoneId.toUInt(),
                    maxParticipants = formState.maxParticipants.toIntOrNull(),
                )

            _uiState.update { it.copy(actionState = EventFormUiState.ActionState.Submitting) }
            eventApi
                .createEvent(request)
                .onSuccess { response ->
                    _events.send(EventFormEvent.EventCreated(Id(response.id)))
                }.onFailure { error ->
                    handleError("Failed to create event", error)
                }
            _uiState.update { it.copy(actionState = EventFormUiState.ActionState.Idle) }
        }
    }

    private fun updateEvent() {
        val formState = _uiState.value
        val eventId = formState.eventIdToEdit ?: return
        if (!validateForm(isCreate = false)) return

        viewModelScope.launch {
            val request =
                UpdateEventRequest(
                    title = formState.title,
                    description = formState.description,
                    startDate = formState.startDate,
                    maxParticipants = formState.maxParticipants.toIntOrNull(),
                )

            _uiState.update { it.copy(actionState = EventFormUiState.ActionState.Submitting) }
            eventApi
                .updateEventDetails(eventId.value, request)
                .onSuccess {
                    _events.send(EventFormEvent.ShowSnackbar("Event updated successfully"))
                    _events.send(EventFormEvent.NavigateBack)
                }.onFailure { error ->
                    handleError("Failed to update event", error)
                }
            _uiState.update { it.copy(actionState = EventFormUiState.ActionState.Idle) }
        }
    }

    private fun validateForm(isCreate: Boolean): Boolean {
        val formState = _uiState.value
        val stateWithErrors =
            formState.copy(
                titleError = if (formState.title.isBlank()) "Title cannot be empty" else null,
                descriptionError =
                    if (formState.description.isBlank()) {
                        "Description cannot be empty"
                    } else {
                        null
                    },
                startDateError =
                    try {
                        LocalDateTime.parse(formState.startDate)
                        null
                    } catch (_: Exception) {
                        "Invalid date format (YYYY-MM-DDTHH:MM:SS)"
                    },
                maxParticipantsError =
                    if (formState.maxParticipants.isNotEmpty()) {
                        val number = formState.maxParticipants.toIntOrNull()
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
                    if (isCreate && formState.zoneId.toUIntOrNull() == null) {
                        "Invalid Zone ID"
                    } else {
                        null
                    },
            )
        _uiState.update { stateWithErrors }
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
        _events.send(EventFormEvent.ShowSnackbar(message))
    }
}
