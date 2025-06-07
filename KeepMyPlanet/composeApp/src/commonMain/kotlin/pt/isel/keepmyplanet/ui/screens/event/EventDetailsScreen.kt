package pt.isel.keepmyplanet.ui.screens.event

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.datetime.LocalDateTime
import pt.isel.keepmyplanet.data.model.EventInfo
import pt.isel.keepmyplanet.domain.common.Description
import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.domain.event.EventStatus
import pt.isel.keepmyplanet.domain.event.Period
import pt.isel.keepmyplanet.domain.event.Title
import pt.isel.keepmyplanet.ui.screens.event.components.toFormattedDateTime

@Suppress("ktlint:standard:function-naming")
@Composable
fun EventDetailsScreen(
    userId: UInt,
    eventId: UInt,
    uiState: EventDetailsUiState,
    onNavigateBack: () -> Unit,
    onNavigateToChat: (EventInfo) -> Unit,
    onLoadEventDetails: (UInt) -> Unit,
    onJoinEvent: (UInt) -> Unit,
    onNavigateToEditEvent: (UInt) -> Unit,
) {
    val event = uiState.event

    val canAttemptToJoin =
        event != null &&
            event.status == "PLANNED" &&
            !(event.participantsIds.contains(userId)) &&
            (event.maxParticipants == null || event.participantsIds.size < event.maxParticipants!!)

    val isJoinButtonEnabled = !uiState.isJoining && canAttemptToJoin
    val isJoiningThisEvent = uiState.isJoining && canAttemptToJoin

    LaunchedEffect(eventId) {
        onLoadEventDetails(eventId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(event?.title ?: "Event Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
            )
        },
    ) { paddingValues ->
        Box(
            modifier =
                Modifier
                    .padding(paddingValues)
                    .fillMaxSize(),
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                )
            } else if (event != null) {
                Column(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Text(
                        text = event.title,
                        style = MaterialTheme.typography.h5,
                    )

                    Text(
                        text = event.description,
                        style = MaterialTheme.typography.body1,
                    )

                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = "Start Date: ${event.startDate.toFormattedDateTime()}",
                            style = MaterialTheme.typography.body2,
                        )
                        // Text("End Date: ${event.endDate}", style = MaterialTheme.typography.body2)
                    }

                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
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

                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            text = "Created: ${event.createdAt.toFormattedDateTime()}",
                            style = MaterialTheme.typography.caption,
                        )
                        Text(
                            text = "Last update: ${event.updatedAt.toFormattedDateTime()}",
                            style = MaterialTheme.typography.caption,
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Button(
                            onClick = { onNavigateToEditEvent(eventId) },
                            modifier = Modifier.weight(1f),
                            enabled = event.organizerId == userId,
                        ) {
                            Text("Edit Event")
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Button(
                            onClick = { onJoinEvent(eventId) },
                            modifier = Modifier.weight(1f),
                            enabled = isJoinButtonEnabled,
                        ) {
                            if (isJoiningThisEvent) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = MaterialTheme.colors.onPrimary,
                                    strokeWidth = 2.dp,
                                )
                            } else {
                                Text("Join Event")
                            }
                        }

                        Button(
                            onClick = {
                                onNavigateToChat(
                                    EventInfo(
                                        id = Id(event.id),
                                        title = Title(event.title),
                                        description = Description(event.description),
                                        period =
                                            Period(
                                                start = LocalDateTime.parse(event.startDate),
                                                end = event.endDate?.let { LocalDateTime.parse(it) },
                                            ),
                                        status = EventStatus.valueOf(event.status.uppercase()),
                                    ),
                                )
                            },
                            modifier = Modifier.weight(1f),
                            enabled = event.participantsIds.contains(userId) || event.organizerId == userId,
                        ) {
                            Text("Chat")
                        }
                    }
                }
            }

            uiState.error?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colors.error,
                    modifier =
                        Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                )
            }
        }
    }
}
