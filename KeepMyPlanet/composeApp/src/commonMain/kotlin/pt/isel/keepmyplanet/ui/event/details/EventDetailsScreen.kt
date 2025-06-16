package pt.isel.keepmyplanet.ui.event.details

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarDuration
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.collectLatest
import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.ui.chat.model.ChatInfo
import pt.isel.keepmyplanet.ui.components.AppTopBar
import pt.isel.keepmyplanet.ui.components.FullScreenLoading
import pt.isel.keepmyplanet.ui.event.EventViewModel
import pt.isel.keepmyplanet.ui.event.details.components.DetailCard
import pt.isel.keepmyplanet.ui.event.details.components.EventActions
import pt.isel.keepmyplanet.ui.event.details.components.InfoRow
import pt.isel.keepmyplanet.ui.event.details.components.ParticipantRow
import pt.isel.keepmyplanet.ui.event.list.components.toFormattedString
import pt.isel.keepmyplanet.ui.event.model.EventScreenEvent

@Suppress("ktlint:standard:function-naming")
@Composable
fun EventDetailsScreen(
    viewModel: EventViewModel,
    userId: UInt,
    eventId: UInt,
    onNavigateBack: () -> Unit,
    onNavigateToChat: (ChatInfo) -> Unit,
    onNavigateToEditEvent: (UInt) -> Unit,
) {
    val uiState by viewModel.detailsUiState.collectAsState()
    val event = uiState.event
    val currentUserId = Id(userId)
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel.events) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is EventScreenEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(event.message, duration = SnackbarDuration.Short)
                }

                is EventScreenEvent.EventDeleted -> {
                    onNavigateBack()
                }

                else -> {}
            }
        }
    }

    LaunchedEffect(eventId) {
        viewModel.loadEventDetails(eventId)
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            AppTopBar(
                title = event?.title?.value ?: "Event Details",
                onNavigateBack = onNavigateBack,
            )
        },
    ) { paddingValues ->
        if (uiState.isLoading) {
            FullScreenLoading(modifier = Modifier.padding(paddingValues))
        } else if (event != null) {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                item {
                    Text(
                        text = event.title.value,
                        style = MaterialTheme.typography.h4,
                        modifier = Modifier.padding(bottom = 8.dp),
                    )
                }

                item {
                    DetailCard(title = "Description") {
                        InfoRow(
                            icon = Icons.Default.Description,
                            text = event.description.value,
                        )
                    }
                }

                item {
                    DetailCard(title = "Details") {
                        uiState.organizer?.let { organizer ->
                            InfoRow(
                                icon = Icons.Default.Person,
                                text = "Organized by ${organizer.name.value}",
                            )
                        }
                        InfoRow(
                            icon = Icons.Default.Schedule,
                            text = "Status: ${event.status.name}",
                        )
                        InfoRow(
                            icon = Icons.Default.CalendarToday,
                            text = "Starts: ${event.period.start.toFormattedString()}",
                        )
                        uiState.zone?.let { zone ->
                            InfoRow(
                                icon = Icons.Default.LocationOn,
                                text =
                                    "Location: Lat ${zone.location.latitude}, Lon ${zone.location.longitude}",
                            )
                        }
                    }
                }

                item {
                    DetailCard(
                        title =
                            "Participants " +
                                "(${uiState.participants.size}/${event.maxParticipants ?: "∞"})",
                    ) {
                        if (uiState.participants.isEmpty()) {
                            Text(
                                "No participants yet.",
                                modifier = Modifier.padding(vertical = 8.dp),
                            )
                        } else {
                            uiState.participants.forEach { participant ->
                                ParticipantRow(participant)
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    EventActions(
                        uiState = uiState,
                        currentUserId = currentUserId,
                        onJoinEvent = { viewModel.joinEvent(eventId) },
                        onLeaveEvent = { viewModel.leaveEvent(eventId) },
                        onNavigateToChat = onNavigateToChat,
                        onNavigateToEditEvent = { onNavigateToEditEvent(eventId) },
                        onCancelEvent = { viewModel.cancelEvent(eventId) },
                        onCompleteEvent = { viewModel.completeEvent(eventId) },
                        onDeleteEvent = { viewModel.deleteEvent(eventId) },
                    )
                }
            }
        }
    }
}
