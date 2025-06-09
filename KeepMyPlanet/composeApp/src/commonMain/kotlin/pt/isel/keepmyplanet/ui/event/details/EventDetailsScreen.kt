package pt.isel.keepmyplanet.ui.event.details

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.ui.chat.model.ChatInfo
import pt.isel.keepmyplanet.ui.components.AppTopBar
import pt.isel.keepmyplanet.ui.event.EventViewModel
import pt.isel.keepmyplanet.ui.event.details.components.EventActions
import pt.isel.keepmyplanet.ui.event.list.components.toFormattedString

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

    LaunchedEffect(eventId) {
        viewModel.loadEventDetails(eventId)
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = event?.title?.value ?: "Event Details",
                onNavigateBack = onNavigateBack,
            )
        },
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (event != null) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
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
                        // Text("End Date: ${event.endDate}", style = MaterialTheme.typography.body2)
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
                        onJoinEvent = { viewModel.joinEvent(eventId) },
                        onLeaveEvent = { viewModel.leaveEvent(eventId) },
                        onNavigateToChat = onNavigateToChat,
                        onNavigateToEditEvent = { onNavigateToEditEvent(eventId) },
                    )
                }
            }

            uiState.error?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colors.error,
                    modifier = Modifier.align(Alignment.Center).padding(16.dp),
                )
            }
        }
    }
}
