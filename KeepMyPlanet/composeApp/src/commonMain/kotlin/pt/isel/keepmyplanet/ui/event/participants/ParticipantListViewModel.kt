package pt.isel.keepmyplanet.ui.event.participants

import pt.isel.keepmyplanet.data.repository.DefaultEventRepository
import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.ui.base.BaseViewModel
import pt.isel.keepmyplanet.ui.event.participants.states.ParticipantListEvent
import pt.isel.keepmyplanet.ui.event.participants.states.ParticipantListUiState

class ParticipantListViewModel(
    private val eventId: Id,
    private val eventRepository: DefaultEventRepository,
) : BaseViewModel<ParticipantListUiState>(ParticipantListUiState()) {

    init {
        loadParticipants()
    }

    fun loadParticipants() {
        launchWithResult(
            onStart = { copy(isLoading = true, error = null) },
            onFinally = { copy(isLoading = false) },
            block = {
                eventRepository.getEventDetailsBundle(eventId)
            },
            onSuccess = { bundle ->
                setState {
                    copy(
                        event = bundle.event,
                        participants = bundle.participants,
                    )
                }
            },
            onError = { error ->
                val message = getErrorMessage("Failed to load participants", error)
                handleErrorWithMessage(message)
                setState { copy(error = message) }
            },
        )
    }

    override fun handleErrorWithMessage(message: String) {
        sendEvent(ParticipantListEvent.ShowSnackbar(message))
    }
}
