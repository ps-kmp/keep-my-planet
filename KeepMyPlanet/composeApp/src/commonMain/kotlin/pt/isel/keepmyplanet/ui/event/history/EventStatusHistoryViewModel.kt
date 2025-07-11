package pt.isel.keepmyplanet.ui.event.history

import pt.isel.keepmyplanet.data.repository.EventApiRepository
import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.ui.base.BaseViewModel
import pt.isel.keepmyplanet.ui.event.history.states.EventStatusHistoryEvent
import pt.isel.keepmyplanet.ui.event.history.states.EventStatusHistoryUiState

class EventStatusHistoryViewModel(
    private val eventRepository: EventApiRepository,
) : BaseViewModel<EventStatusHistoryUiState>(EventStatusHistoryUiState()) {
    fun loadHistory(eventId: Id) {
        launchWithResult(
            onStart = { copy(isLoading = true, error = null) },
            onFinally = { copy(isLoading = false) },
            block = { eventRepository.getEventStatusHistory(eventId) },
            onSuccess = { history ->
                setState { copy(history = history, isLoading = false) }
            },
            onError = {
                if (currentState.history.isEmpty()) {
                    setState { copy(error = getErrorMessage("Failed to load history", it)) }
                } else {
                    handleErrorWithMessage(getErrorMessage("Failed to refresh history", it))
                }
            },
        )
    }

    override fun handleErrorWithMessage(message: String) {
        sendEvent(EventStatusHistoryEvent.ShowSnackbar(message))
    }
}
