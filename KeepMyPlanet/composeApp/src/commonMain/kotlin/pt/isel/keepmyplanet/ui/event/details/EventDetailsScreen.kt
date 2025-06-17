package pt.isel.keepmyplanet.ui.event.details

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarDuration
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Text
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
import pt.isel.keepmyplanet.ui.event.details.components.EventActions
import pt.isel.keepmyplanet.ui.event.list.components.toFormattedString

@Suppress("ktlint:standard:function-naming")
@Composable
fun EventDetailsScreen(
    viewModel: EventDetailsViewModel,
    userId: UInt,
    eventId: Id,
    onNavigateBack: () -> Unit,
    onNavigateToChat: (ChatInfo) -> Unit,
    onNavigateToEditEvent: (Id) -> Unit,
) {
    val uiState by viewModel.detailsUiState.collectAsState()
    val event = uiState.event
    val currentUserId = Id(userId)
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel.events) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is EventDetailsScreenEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(event.message, duration = SnackbarDuration.Short)
                }

                is EventDetailsScreenEvent.EventDeleted -> {
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
            Column(
                modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = event.title.value,
                    style = MaterialTheme.typography.h5,
                )

                Text(
                    text = event.description.value,
                    style = MaterialTheme.typography.body1,
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Start Date: ${event.period.start.toFormattedString()}",
                        style = MaterialTheme.typography.body2,
                    )
                    event.period.end?.let {
                        Text(
                            text = "End Date: ${it.toFormattedString()}",
                            style = MaterialTheme.typography.body2,
                        )
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Status: ${event.status}",
                        style = MaterialTheme.typography.body2,
                        color = MaterialTheme.colors.secondary,
                    )
                    event.maxParticipants?.let {
                        Text(
                            text = "Participants: ${event.participantsIds.size}/$it",
                            style = MaterialTheme.typography.body2,
                        )
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Created: ${event.createdAt.toFormattedString()}",
                        style = MaterialTheme.typography.caption,
                    )
                    Text(
                        text = "Last update: ${event.updatedAt.toFormattedString()}",
                        style = MaterialTheme.typography.caption,
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                EventActions(
                    uiState = uiState,
                    currentUserId = currentUserId,
                    onJoinEvent = viewModel::joinEvent,
                    onLeaveEvent = viewModel::leaveEvent,
                    onNavigateToChat = onNavigateToChat,
                    onNavigateToEditEvent = { onNavigateToEditEvent(event.id) },
                    onCancelEvent = viewModel::cancelEvent,
                    onCompleteEvent = viewModel::completeEvent,
                    onDeleteEvent = viewModel::deleteEvent,
                )
            }
        }
    }
}
