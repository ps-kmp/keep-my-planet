package pt.isel.keepmyplanet.ui.event.participants

import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import pt.isel.keepmyplanet.data.repository.EventApiRepository
import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.ui.base.BaseViewModel
import pt.isel.keepmyplanet.ui.event.participants.states.ParticipantListEvent
import pt.isel.keepmyplanet.ui.event.participants.states.ParticipantListUiState

class ParticipantListViewModel(
    private val eventRepository: EventApiRepository,
) : BaseViewModel<ParticipantListUiState>(ParticipantListUiState()) {
    fun loadParticipants(eventId: Id) {
        viewModelScope.launch {
            setState { copy(isLoading = true, error = null) }
            eventRepository.getEventDetailsBundle(eventId).collectLatest { result ->
                result
                    .onSuccess { bundle ->
                        setState {
                            copy(
                                isLoading = false,
                                event = bundle.event,
                                participants = bundle.participants,
                            )
                        }
                    }.onFailure { error ->
                        val message = getErrorMessage("Failed to load participants", error)
                        handleErrorWithMessage(message)
                        if (currentState.event == null) {
                            setState { copy(isLoading = false, error = message) }
                        } else {
                            setState { copy(isLoading = false) }
                        }
                    }
            }
        }
    }

    override fun handleErrorWithMessage(message: String) {
        sendEvent(ParticipantListEvent.ShowSnackbar(message))
    }
}
