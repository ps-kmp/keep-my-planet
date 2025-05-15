package pt.isel.keepmyplanet.ui.screens.event

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import pt.isel.keepmyplanet.data.model.EventInfo
import pt.isel.keepmyplanet.data.model.UserInfo
import pt.isel.keepmyplanet.data.service.EventService
import pt.isel.keepmyplanet.dto.event.CreateEventRequest
import pt.isel.keepmyplanet.dto.event.UpdateEventRequest

/*class EventViewModel(
    private val eventService: EventService,
    val user: UserInfo,
    val event: EventInfo,
) : ViewModel() {
    private val _uiState = MutableStateFlow(EventUiState(user = user, event = event))
    val uiState: StateFlow<EventUiState> = _uiState.asStateFlow()

    private val _events = Channel<EventScreenEvent>(Channel.BUFFERED)
    val events: Flow<EventScreenEvent> = _events.receiveAsFlow()
}*/
class EventViewModel(
    private val eventService: EventService,
    private val user: UserInfo,
) : ViewModel() {
    private val _listUiState = MutableStateFlow(EventListUiState())
    val listUiState: StateFlow<EventListUiState> = _listUiState.asStateFlow()

    private val _detailsUiState = MutableStateFlow(EventDetailsUiState())
    val detailsUiState: StateFlow<EventDetailsUiState> = _detailsUiState.asStateFlow()

    private val _events = Channel<EventScreenEvent>(Channel.BUFFERED)
    val events: Flow<EventScreenEvent> = _events.receiveAsFlow()

    init {
        loadEvents()
    }

    fun loadEvents(query: String? = null) {
        viewModelScope.launch {
            _listUiState.value = _listUiState.value.copy(isLoading = true)
            eventService
                .searchAllEvents(query)
                .onSuccess { events ->
                    _listUiState.value =
                        _listUiState.value.copy(
                            events =
                                events.map { response ->
                                    EventInfo(
                                        id = response.id,
                                        title = response.title,
                                        description = response.description,
                                        startDate = response.startDate,
                                        endDate = response.endDate,
                                        status = response.status,
                                    )
                                },
                            isLoading = false,
                            error = null,
                        )
                }.onFailure {
                    _listUiState.value =
                        _listUiState.value.copy(
                            isLoading = false,
                            error = "Erro ao carregar eventos",
                        )
                }
        }
    }

    fun createEvent(request: CreateEventRequest) {
        viewModelScope.launch {
            _listUiState.value = _listUiState.value.copy(isLoading = true)
            eventService
                .createEvent(request, user.id)
                .onSuccess {
                    loadEvents()
                    _events.send(EventScreenEvent.ShowSnackbar("Evento criado com sucesso"))
                }.onFailure {
                    _listUiState.value =
                        _listUiState.value.copy(
                            isLoading = false,
                            error = "Erro ao criar evento",
                        )
                }
        }
    }

    fun loadEventDetails(eventId: UInt) {
        viewModelScope.launch {
            _detailsUiState.value = _detailsUiState.value.copy(isLoading = true)
            eventService
                .getEventDetails(eventId)
                .onSuccess { event ->
                    _detailsUiState.value =
                        _detailsUiState.value.copy(
                            event = event,
                            isLoading = false,
                            error = null,
                        )
                }.onFailure {
                    _detailsUiState.value =
                        _detailsUiState.value.copy(
                            isLoading = false,
                            error = "Erro ao carregar detalhes do evento",
                        )
                }
        }
    }

    fun joinEvent(eventId: UInt) {
        viewModelScope.launch {
            _detailsUiState.value = _detailsUiState.value.copy(isJoining = true)
            eventService
                .joinEvent(eventId, user.id)
                .onSuccess {
                    loadEventDetails(eventId)
                    _events.send(EventScreenEvent.ShowSnackbar("Aderiu ao evento com sucesso"))
                }.onFailure {
                    _detailsUiState.value =
                        _detailsUiState.value.copy(
                            isJoining = false,
                            error = "Erro ao aderir ao evento",
                        )
                }
        }
    }

    fun updateEvent(
        eventId: UInt,
        request: UpdateEventRequest,
    ) {
        viewModelScope.launch {
            _detailsUiState.value = _detailsUiState.value.copy(isEditing = true)
            eventService
                .updateEventDetails(eventId, user.id, request)
                .onSuccess {
                    loadEventDetails(eventId)
                    _events.send(EventScreenEvent.ShowSnackbar("Evento atualizado com sucesso"))
                }.onFailure {
                    _detailsUiState.value =
                        _detailsUiState.value.copy(
                            isEditing = false,
                            error = "Erro ao atualizar evento",
                        )
                }
        }
    }

    fun leaveEvent(eventId: UInt) {
        viewModelScope.launch {
            _detailsUiState.value = _detailsUiState.value.copy(isLeaving = true)
            eventService
                .leaveEvent(eventId, user.id)
                .onSuccess {
                    loadEventDetails(eventId)
                    _events.send(EventScreenEvent.ShowSnackbar("Saiu do evento com sucesso"))
                }.onFailure {
                    _detailsUiState.value =
                        _detailsUiState.value.copy(
                            isLeaving = false,
                            error = "Erro ao sair do evento",
                        )
                }
        }
    }
}
