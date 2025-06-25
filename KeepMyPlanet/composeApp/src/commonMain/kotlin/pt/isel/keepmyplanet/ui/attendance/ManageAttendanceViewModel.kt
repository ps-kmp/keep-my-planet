package pt.isel.keepmyplanet.ui.attendance

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import pt.isel.keepmyplanet.data.api.EventApi
import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.dto.event.CheckInRequest
import pt.isel.keepmyplanet.mapper.event.toEvent
import pt.isel.keepmyplanet.mapper.user.toUserInfo
import pt.isel.keepmyplanet.ui.attendance.states.ManageAttendanceEvent
import pt.isel.keepmyplanet.ui.attendance.states.ManageAttendanceUiState
import pt.isel.keepmyplanet.ui.viewmodel.BaseViewModel

class ManageAttendanceViewModel(
    private val eventApi: EventApi,
    private val eventId: Id,
) : BaseViewModel<ManageAttendanceUiState>(ManageAttendanceUiState()) {
    init {
        loadInitialData()
    }

    override fun handleErrorWithMessage(message: String) {
        sendEvent(ManageAttendanceEvent.ShowSnackbar(message))
    }

    fun loadInitialData() {
        viewModelScope.launch {
            setState { copy(isLoading = true, error = null) }
            val result =
                runCatching {
                    coroutineScope {
                        val detailsDeferred = async { eventApi.getEventDetails(eventId.value) }
                        val participantsDeferred =
                            async { eventApi.getEventParticipants(eventId.value) }
                        val attendeesDeferred = async { eventApi.getEventAttendees(eventId.value) }
                        Triple(
                            detailsDeferred.await().getOrThrow(),
                            participantsDeferred.await().getOrThrow(),
                            attendeesDeferred.await().getOrThrow(),
                        )
                    }
                }
            result
                .onSuccess { (eventDto, participantsDto, attendeesDto) ->
                    setState {
                        copy(
                            event = eventDto.toEvent(),
                            participants = participantsDto.map { it.toUserInfo() },
                            attendees = attendeesDto.map { it.toUserInfo() },
                        )
                    }
                }.onFailure { e ->
                    setState { copy(error = getErrorMessage("Failed to load attendance data", e)) }
                }
            setState { copy(isLoading = false) }
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
            sendEvent(ManageAttendanceEvent.ShowSnackbar("Invalid QR code scanned."))
        }
    }

    private fun checkInUser(scannedUserId: Id) {
        if (currentState.isCheckingIn) return
        val participantInfo = currentState.participants.find { it.id == scannedUserId }
        if (participantInfo == null) {
            sendEvent(ManageAttendanceEvent.ShowSnackbar("User is not registered for this event."))
            return
        }
        val request = CheckInRequest(userId = scannedUserId.value)
        launchWithResult(
            onStart = { copy(isCheckingIn = true) },
            onFinally = { copy(isCheckingIn = false) },
            block = { eventApi.checkInUser(eventId.value, request) },
            onSuccess = {
                sendEvent(
                    ManageAttendanceEvent.ShowSnackbar(
                        "User ${participantInfo.name.value} checked in successfully!",
                    ),
                )
                refreshAttendees()
            },
            onError = { handleErrorWithMessage(getErrorMessage("Check-in failed", it)) },
        )
    }

    private fun refreshAttendees() {
        launchWithResult(
            block = { eventApi.getEventAttendees(eventId.value) },
            onSuccess = { setState { copy(attendees = it.map { a -> a.toUserInfo() }) } },
            onError = {
                handleErrorWithMessage(
                    getErrorMessage("Failed to refresh attendee list", it),
                )
            },
        )
    }
}
