package pt.isel.keepmyplanet.ui.user.stats

import pt.isel.keepmyplanet.data.api.EventApi
import pt.isel.keepmyplanet.data.mapper.toListItem
import pt.isel.keepmyplanet.ui.base.BaseViewModel
import pt.isel.keepmyplanet.ui.user.stats.model.UserStatsEvent
import pt.isel.keepmyplanet.ui.user.stats.model.UserStatsUiState

class UserStatsViewModel(
    private val eventApi: EventApi,
) : BaseViewModel<UserStatsUiState>(UserStatsUiState()) {
    init {
        loadAttendedEvents(isRefresh = true)
    }

    override fun handleErrorWithMessage(message: String) {
        sendEvent(UserStatsEvent.ShowSnackbar(message))
    }

    fun loadAttendedEvents(isRefresh: Boolean = false) {
        val offset = if (isRefresh) 0 else currentState.offset

        if (isRefresh.not() && (currentState.isLoading || currentState.isAddingMore)) return

        launchWithResult(
            onStart = {
                copy(
                    isLoading = isRefresh,
                    isAddingMore = !isRefresh,
                    error = if (isRefresh) null else error,
                )
            },
            onFinally = { copy(isLoading = false, isAddingMore = false) },
            block = { eventApi.getAttendedEvents(limit = currentState.limit, offset = offset) },
            onSuccess = { response ->
                setState {
                    val currentEvents = if (isRefresh) emptyList() else attendedEvents
                    val newEvents = response.map { it.toListItem() }
                    copy(
                        attendedEvents = (currentEvents + newEvents).distinctBy { it.id },
                        offset = if (isRefresh) newEvents.size else this.offset + newEvents.size,
                        hasMorePages = newEvents.size == this.limit,
                    )
                }
            },
            onError = {
                if (isRefresh) {
                    setState { copy(error = getErrorMessage("Failed to load stats", it)) }
                } else {
                    handleErrorWithMessage(getErrorMessage("Failed to load more events", it))
                }
            },
        )
    }

    fun loadNextPage() {
        val state = currentState
        if (state.isLoading || state.isAddingMore || !state.hasMorePages) return
        loadAttendedEvents()
    }
}
