package pt.isel.keepmyplanet.ui.event.stats

import pt.isel.keepmyplanet.data.repository.EventApiRepository
import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.ui.base.BaseViewModel
import pt.isel.keepmyplanet.ui.event.stats.states.EventStatsEvent
import pt.isel.keepmyplanet.ui.event.stats.states.EventStatsUiState

class EventStatsViewModel(
    private val eventRepository: EventApiRepository,
) : BaseViewModel<EventStatsUiState>(EventStatsUiState()) {
    fun loadStats(eventId: Id) {
        launchWithResult(
            onStart = { copy(isLoading = true, error = null) },
            onFinally = { copy(isLoading = false) },
            block = { eventRepository.getEventStats(eventId) },
            onSuccess = { stats -> setState { copy(stats = stats) } },
            onError = { error ->
                val message = getErrorMessage("Failed to load event statistics", error)
                setState { copy(error = message) }
            },
        )
    }

    override fun handleErrorWithMessage(message: String) {
        sendEvent(EventStatsEvent.ShowSnackbar(message))
    }
}
