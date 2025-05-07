package pt.isel.keepmyplanet.ui.screens.event

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import pt.isel.keepmyplanet.data.model.EventInfo
import pt.isel.keepmyplanet.data.model.UserInfo
import pt.isel.keepmyplanet.data.service.EventService

class EventViewModel(
    private val eventService: EventService,
    val user: UserInfo,
    val event: EventInfo,
) : ViewModel() {
    private val _uiState = MutableStateFlow(EventUiState(user = user, event = event))
    val uiState: StateFlow<EventUiState> = _uiState.asStateFlow()

    private val _events = Channel<EventScreenEvent>(Channel.BUFFERED)
    val events: Flow<EventScreenEvent> = _events.receiveAsFlow()
}
