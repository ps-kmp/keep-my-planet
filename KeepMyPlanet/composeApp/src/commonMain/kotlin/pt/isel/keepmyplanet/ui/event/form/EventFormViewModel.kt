package pt.isel.keepmyplanet.ui.event.form

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
import pt.isel.keepmyplanet.ui.event.model.EventFormUiState

class EventFormViewModel(
    private val eventApi: EventApi,
) : ViewModel() {
    private val _formUiState = MutableStateFlow(EventFormUiState())
    val formUiState: StateFlow<EventFormUiState> = _formUiState.asStateFlow()

    private val _events = Channel<EventFormScreenEvent>(Channel.BUFFERED)
    val events: Flow<EventFormScreenEvent> = _events.receiveAsFlow()

    private var eventIdToEdit: Id? = null

    fun prepareForCreate(zoneId: Id?) {
        eventIdToEdit = null
        _formUiState.value =
            EventFormUiState(
                zoneId = zoneId?.value?.toString() ?: "",
                isEditMode = false,
            )
    }

    fun prepareForEdit(eventId: Id) {
        eventIdToEdit = eventId
        _formUiState.update { it.copy(isEditMode = true, isLoading = true) }
        viewModelScope.launch {
            eventApi
                .getEventDetails(eventId.value)
                .onSuccess { event ->
                    _formUiState.update {
                        it.copy(
                            isLoading = false,
                            title = event.title,
                            description = event.description,
                            startDate = event.startDate,
                            maxParticipants = event.maxParticipants?.toString() ?: "",
                        )
                    }
                }.onFailure { error ->
                    _formUiState.update { it.copy(isLoading = false) }
                    handleError("Failed to load event for editing", error)
                }
        }
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

    fun onZoneIdChanged(zoneId: String) {
        _formUiState.update { it.copy(zoneId = zoneId, zoneIdError = null) }
    }

    fun submit() {
        if (formUiState.value.isEditMode) {
            updateEvent()
        } else {
            createEvent()
        }
    }

    private fun createEvent() {
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
                    _events.send(EventFormScreenEvent.EventCreated(Id(response.id)))
                }.onFailure { error ->
                    handleError("Failed to create event", error)
                }
            _formUiState.update { it.copy(isSubmitting = false) }
        }
    }

    private fun updateEvent() {
        val eventId = eventIdToEdit ?: return
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
                .updateEventDetails(eventId.value, request)
                .onSuccess {
                    _events.send(EventFormScreenEvent.ShowSnackbar("Event updated successfully"))
                    _events.send(EventFormScreenEvent.EventUpdated)
                    _events.send(EventFormScreenEvent.NavigateBack)
                }.onFailure { error ->
                    handleError("Failed to update event", error)
                }
            _formUiState.update { it.copy(isSubmitting = false) }
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
                        "Invalid date format (YYYY-MM-DDTHH:MM:SS)"
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
        _events.send(EventFormScreenEvent.ShowSnackbar(message))
    }
}
