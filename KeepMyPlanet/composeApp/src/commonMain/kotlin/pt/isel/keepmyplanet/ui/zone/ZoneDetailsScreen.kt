package pt.isel.keepmyplanet.ui.zone

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Report
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.rememberAsyncImagePainter
import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.domain.zone.ZoneStatus
import pt.isel.keepmyplanet.ui.common.FullScreenLoading
import pt.isel.keepmyplanet.ui.components.AppTopBar
import pt.isel.keepmyplanet.ui.components.DetailCard
import pt.isel.keepmyplanet.ui.components.ErrorState
import pt.isel.keepmyplanet.ui.components.InfoRow
import pt.isel.keepmyplanet.ui.components.StatusBadge
import pt.isel.keepmyplanet.ui.components.getSeverityColor
import pt.isel.keepmyplanet.ui.components.getStatusColor

@Composable
fun ZoneDetailsScreen(
    viewModel: ZoneDetailsViewModel,
    zoneId: Id,
    onNavigateToCreateEvent: (zoneId: Id) -> Unit,
    onNavigateToEventDetails: (eventId: Id) -> Unit,
    onNavigateBack: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val zone = uiState.zone

    LaunchedEffect(zoneId) {
        viewModel.loadZoneDetails(zoneId)
    }

    Scaffold(
        topBar = { AppTopBar(title = "Zone Details", onNavigateBack = onNavigateBack) },
    ) { paddingValues ->
        Box(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentAlignment = Alignment.Center,
        ) {
            when {
                uiState.isLoading -> FullScreenLoading()
                uiState.error != null ->
                    ErrorState(
                        message = uiState.error!!,
                        onRetry = { viewModel.loadZoneDetails(zoneId) },
                    )

                zone != null -> {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        Text("Zone #${zone.id.value}", style = MaterialTheme.typography.h4)

                        DetailCard(title = "Details") {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                InfoRow(
                                    icon = Icons.Default.Category,
                                    text = zone.description.value,
                                )
                                InfoRow(
                                    icon = Icons.Default.Report,
                                    text = "Reported by User #${zone.reporterId.value}",
                                )
                            }
                        }

                        DetailCard(title = "Status & Severity") {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                StatusBadge(
                                    text = zone.status.name.replace('_', ' '),
                                    backgroundColor = getStatusColor(zone.status),
                                )
                                StatusBadge(
                                    text = zone.zoneSeverity.name,
                                    backgroundColor = getSeverityColor(zone.zoneSeverity),
                                    icon = Icons.Default.ErrorOutline,
                                )
                            }
                        }

                        if (uiState.photoUrls.isNotEmpty()) {
                            DetailCard(title = "Photos") {
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    items(uiState.photoUrls) { url ->
                                        Card(
                                            modifier = Modifier.size(120.dp),
                                            shape = RoundedCornerShape(8.dp),
                                            elevation = 2.dp,
                                        ) {
                                            Image(
                                                painter = rememberAsyncImagePainter(model = url),
                                                contentDescription = "Zone Photo",
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize(),
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        if (zone.eventId != null) {
                            Button(
                                onClick = { onNavigateToEventDetails(zone.eventId!!) },
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Icon(
                                    Icons.Default.CalendarToday,
                                    contentDescription = null,
                                    modifier = Modifier.padding(end = 8.dp),
                                )
                                Text("View Associated Event")
                            }
                        } else if (zone.status == ZoneStatus.REPORTED) {
                            Button(
                                onClick = { onNavigateToCreateEvent(zone.id) },
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Icon(
                                    Icons.Default.CalendarToday,
                                    contentDescription = null,
                                    modifier = Modifier.padding(end = 8.dp),
                                )
                                Text("Create Cleanup Event")
                            }
                        }
                    }
                }

                else -> {
                    Text("Could not load zone details.")
                }
            }
        }
    }
}
