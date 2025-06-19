package pt.isel.keepmyplanet.ui.event.attendance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pt.isel.keepmyplanet.data.api.EventApi
import pt.isel.keepmyplanet.data.http.ApiException
import pt.isel.keepmyplanet.data.mapper.toUserInfo
import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.dto.event.CheckInRequest
import pt.isel.keepmyplanet.mapper.event.toEvent
import pt.isel.keepmyplanet.ui.event.attendance.model.ManageAttendanceUiState

class ManageAttendanceViewModel(
    private val eventApi: EventApi,
    private val eventId: Id,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ManageAttendanceUiState())
    val uiState: StateFlow<ManageAttendanceUiState> = _uiState.asStateFlow()

    init {
        loadInitialData()
    }

    fun loadInitialData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val detailsResult = eventApi.getEventDetails(eventId.value)
            val participantsResult = eventApi.getEventParticipants(eventId.value)
            val attendeesResult = eventApi.getEventAttendees(eventId.value)

            detailsResult.onFailure {
                _uiState.update { s ->
                    s.copy(
                        error = "Failed to load event details",
                    )
                }
            }
            participantsResult.onFailure {
                _uiState.update {
                        s ->
                    s.copy(error = "Failed to load participants")
                }
            }
            attendeesResult.onFailure {
                _uiState.update {
                        s ->
                    s.copy(error = "Failed to load attendees")
                }
            }

            _uiState.update {
                it.copy(
                    isLoading = false,
                    event = detailsResult.getOrNull()?.toEvent(),
                    participants =
                        participantsResult.getOrNull()?.map { p -> p.toUserInfo() }
                            ?: emptyList(),
                    attendees =
                        attendeesResult.getOrNull()?.map { a -> a.toUserInfo() }
                            ?: emptyList(),
                )
            }
        }
    }

    fun checkInUser(scannedUserId: Id) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    checkInStatusMessage = "Checking in user ${scannedUserId.value}...",
                )
            }

            val request = CheckInRequest(userId = scannedUserId.value)
            eventApi.checkInUser(eventId.value, request)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            checkInStatusMessage = "User ${scannedUserId.value} checked in successfully!",
                        )
                    }
                    refreshAttendees()
                }
                .onFailure { error ->
                    val errorMessage = (error as? ApiException)?.error?.message ?: "Check-in failed"
                    _uiState.update { it.copy(checkInStatusMessage = "Error: $errorMessage") }
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

    fun clearStatusMessage() {
        _uiState.update { it.copy(checkInStatusMessage = null) }
    }
}
