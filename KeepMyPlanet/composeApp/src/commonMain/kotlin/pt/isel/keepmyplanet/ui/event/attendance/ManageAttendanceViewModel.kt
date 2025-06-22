package pt.isel.keepmyplanet.ui.event.attendance

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
import pt.isel.keepmyplanet.dto.event.CheckInRequest
import pt.isel.keepmyplanet.mapper.event.toEvent
import pt.isel.keepmyplanet.ui.event.attendance.model.ManageAttendanceEvent
import pt.isel.keepmyplanet.ui.event.attendance.model.ManageAttendanceUiState

class ManageAttendanceViewModel(
    private val eventApi: EventApi,
    private val eventId: Id,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ManageAttendanceUiState())
    val uiState: StateFlow<ManageAttendanceUiState> = _uiState.asStateFlow()

    private val _events = Channel<ManageAttendanceEvent>(Channel.BUFFERED)
    val events: Flow<ManageAttendanceEvent> = _events.receiveAsFlow()

    init {
        loadInitialData()
    }

    fun loadInitialData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result =
                runCatching {
                    coroutineScope {
                        val detailsDeferred = async { eventApi.getEventDetails(eventId.value) }
                        val participantsDeferred =
                            async { eventApi.getEventParticipants(eventId.value) }
                        val attendeesDeferred =
                            async { eventApi.getEventAttendees(eventId.value) }

                        val details = detailsDeferred.await().getOrThrow()
                        val participants = participantsDeferred.await().getOrThrow()
                        val attendees = attendeesDeferred.await().getOrThrow()

                        Triple(details, participants, attendees)
                    }
                }

            result
                .onSuccess { (eventDto, participantsDto, attendeesDto) ->
                    _uiState.update {
                        it.copy(
                            event = eventDto.toEvent(),
                            participants = participantsDto.map { p -> p.toUserInfo() },
                            attendees = attendeesDto.map { a -> a.toUserInfo() },
                        )
                    }
                }.onFailure { e ->
                    val errorMessage = getErrorMessage("Failed to load attendance data", e)
                    _uiState.update { it.copy(error = errorMessage) }
                }

            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun onQrCodeScanned(qrData: String) {
        val userId =
            try {
                Id(qrData.toUInt())
            } catch (_: NumberFormatException) {
                null
            }

        if (userId != null) {
            checkInUser(userId)
        } else {
            viewModelScope.launch {
                _events.send(ManageAttendanceEvent.ShowSnackbar("Invalid QR code scanned."))
            }
        }
    }

    private fun checkInUser(scannedUserId: Id) {
        viewModelScope.launch {
            val request = CheckInRequest(userId = scannedUserId.value)
            eventApi
                .checkInUser(eventId.value, request)
                .onSuccess {
                    _events.send(
                        ManageAttendanceEvent.ShowSnackbar(
                            "User ${scannedUserId.value} checked in successfully!",
                        ),
                    )
                    refreshAttendees()
                }.onFailure { error ->
                    handleError("Check-in failed", error)
                }
        }
    }

    private fun refreshAttendees() {
        viewModelScope.launch {
            eventApi.getEventAttendees(eventId.value).onSuccess { response ->
                _uiState.update {
                    it.copy(attendees = response.map { a -> a.toUserInfo() })
                }
            }
        }
    }

    private fun getErrorMessage(
        prefix: String,
        exception: Throwable,
    ): String =
        when (exception) {
            is ApiException -> exception.error.message
            else -> "$prefix: ${exception.message ?: "Unknown error"}"
        }

    private suspend fun handleError(
        prefix: String,
        exception: Throwable,
    ) {
        val errorMsg = getErrorMessage(prefix, exception)
        _events.send(ManageAttendanceEvent.ShowSnackbar(errorMsg))
    }
}
