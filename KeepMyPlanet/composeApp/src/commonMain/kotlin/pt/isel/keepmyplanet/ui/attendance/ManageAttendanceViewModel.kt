package pt.isel.keepmyplanet.ui.attendance

import kotlinx.coroutines.launch
import pt.isel.keepmyplanet.data.repository.DefaultEventRepository
import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.dto.event.CheckInRequest
import pt.isel.keepmyplanet.ui.attendance.states.ManageAttendanceEvent
import pt.isel.keepmyplanet.ui.attendance.states.ManageAttendanceUiState
import pt.isel.keepmyplanet.ui.base.BaseViewModel

class ManageAttendanceViewModel(
    private val eventRepository: DefaultEventRepository,
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
                    val bundle = eventRepository.getEventDetailsBundle(eventId).getOrThrow()
                    val attendees = eventRepository.getEventAttendees(eventId).getOrThrow()
                    Pair(bundle, attendees)
                }
            result
                .onSuccess { (bundle, attendees) ->
                    setState {
                        copy(
                            event = bundle.event,
                            participants = bundle.participants,
                            attendees = attendees,
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
            block = { eventRepository.checkInUser(eventId, request) },
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
            block = { eventRepository.getEventAttendees(eventId) },
            onSuccess = { setState { copy(attendees = it) } },
            onError = {
                handleErrorWithMessage(
                    getErrorMessage("Failed to refresh attendee list", it),
                )
            },
        )
    }
}
