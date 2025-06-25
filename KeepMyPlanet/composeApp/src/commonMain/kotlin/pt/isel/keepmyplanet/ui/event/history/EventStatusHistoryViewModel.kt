package pt.isel.keepmyplanet.ui.event.history

import pt.isel.keepmyplanet.data.api.EventApi
import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.ui.event.history.states.EventStatusHistoryEvent
import pt.isel.keepmyplanet.ui.event.history.states.EventStatusHistoryUiState
import pt.isel.keepmyplanet.ui.viewmodel.BaseViewModel

class EventStatusHistoryViewModel(
    private val eventApi: EventApi,
) : BaseViewModel<EventStatusHistoryUiState>(EventStatusHistoryUiState()) {
    fun loadHistory(eventId: Id) {
        launchWithResult(
            onStart = { copy(isLoading = true, error = null) },
            onFinally = { copy(isLoading = false) },
            block = { eventApi.getEventStatusHistory(eventId.value) },
            onSuccess = { history ->
                setState { copy(history = history) }
            },
        )
    }

    override fun handleErrorWithMessage(message: String) {
        setState { copy(error = message) }
        sendEvent(EventStatusHistoryEvent.ShowSnackbar(message))
    }
}
