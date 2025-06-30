package pt.isel.keepmyplanet.ui.event.history

import pt.isel.keepmyplanet.data.api.EventApi
import pt.isel.keepmyplanet.data.repository.EventStatusHistoryCacheRepository
import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.ui.event.history.states.EventStatusHistoryEvent
import pt.isel.keepmyplanet.ui.event.history.states.EventStatusHistoryUiState
import pt.isel.keepmyplanet.ui.viewmodel.BaseViewModel

class EventStatusHistoryViewModel(
    private val eventApi: EventApi,
    private val cacheRepository: EventStatusHistoryCacheRepository,
) : BaseViewModel<EventStatusHistoryUiState>(EventStatusHistoryUiState()) {
    fun loadHistory(eventId: Id) {
        launchWithResult(
            onStart = {
                val cachedHistory = cacheRepository.getHistoryByEventId(eventId)
                if (cachedHistory.isNotEmpty()) {
                    copy(history = cachedHistory, isLoading = true)
                } else {
                    copy(isLoading = true, error = null)
                }
            },
            onFinally = { copy(isLoading = false) },
            block = { eventApi.getEventStatusHistory(eventId.value) },
            onSuccess = { history ->
                cacheRepository.insertHistory(eventId, history)
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
