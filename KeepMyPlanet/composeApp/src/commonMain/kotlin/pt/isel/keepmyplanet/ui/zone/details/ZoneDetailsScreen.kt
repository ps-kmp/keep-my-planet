package pt.isel.keepmyplanet.ui.zone.details

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Report
import androidx.compose.material.icons.filled.Update
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.rememberAsyncImagePainter
import kotlinx.coroutines.flow.collectLatest
import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.domain.zone.ZoneStatus
import pt.isel.keepmyplanet.ui.components.AppTopBar
import pt.isel.keepmyplanet.ui.components.ConfirmActionDialog
import pt.isel.keepmyplanet.ui.components.DetailCard
import pt.isel.keepmyplanet.ui.components.ErrorState
import pt.isel.keepmyplanet.ui.components.FullScreenLoading
import pt.isel.keepmyplanet.ui.components.InfoRow
import pt.isel.keepmyplanet.ui.components.LoadingOutlinedButton
import pt.isel.keepmyplanet.ui.components.StatusBadge
import pt.isel.keepmyplanet.ui.components.getSeverityColor
import pt.isel.keepmyplanet.ui.components.getStatusColor
import pt.isel.keepmyplanet.ui.theme.primaryLight
import pt.isel.keepmyplanet.ui.zone.details.states.ZoneDetailsEvent
import pt.isel.keepmyplanet.ui.zone.details.states.ZoneDetailsUiState

@Composable
fun ZoneDetailsScreen(
    viewModel: ZoneDetailsViewModel,
    zoneId: Id,
    onNavigateToCreateEvent: (zoneId: Id) -> Unit,
    onNavigateToEventDetails: (eventId: Id) -> Unit,
    onNavigateToUpdateZone: (zoneId: Id) -> Unit,
    onNavigateBack: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val zone = uiState.zone
    val snackbarHostState = remember { SnackbarHostState() }

    val showDeleteDialog = remember { mutableStateOf(false) }

    if (showDeleteDialog.value) {
        ConfirmActionDialog(
            showDialog = showDeleteDialog,
            onConfirm = viewModel::deleteZone,
            title = "Delete Zone?",
            text =
                "Are you sure you want to permanently delete this zone report? " +
                    "This action cannot be undone.",
        )
    }

    LaunchedEffect(viewModel.events) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is ZoneDetailsEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
                is ZoneDetailsEvent.ZoneDeleted -> onNavigateBack()
            }
        }
    }

    LaunchedEffect(zoneId) {
        viewModel.loadZoneDetails(zoneId)
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = { AppTopBar(title = "Zone Details", onNavigateBack = onNavigateBack) },
    ) { paddingValues ->
        Box(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentAlignment = Alignment.Center,
        ) {
            when {
                uiState.isLoading && zone == null -> FullScreenLoading()
                uiState.error != null ->
                    ErrorState(
                        message = uiState.error!!,
                        onRetry = { viewModel.loadZoneDetails(zoneId) },
                    )

                zone != null -> {
                    Column(
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        DetailCard(title = "Description") {
                            Text(
                                text = zone.description.value,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }

                        DetailCard(title = "Information") {
                            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                InfoRow(
                                    icon = Icons.Default.Report,
                                    text = "Reported by User #${zone.reporterId.value}",
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Flag,
                                        contentDescription = "Status",
                                        modifier = Modifier.padding(end = 16.dp),
                                        tint = primaryLight
                                    )
                                    StatusBadge(
                                        text = zone.status.name.replace('_', ' '),
                                        backgroundColor = getStatusColor(zone.status),
                                    )
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = "Severity",
                                        modifier = Modifier.padding(end = 16.dp),
                                        tint = primaryLight,
                                    )
                                    StatusBadge(
                                        text = zone.zoneSeverity.name,
                                        backgroundColor = getSeverityColor(zone.zoneSeverity),
                                        icon = Icons.Default.Warning,
                                    )
                                }
                            }
                        }

                        if (uiState.photoModels.isNotEmpty()) {
                            DetailCard(title = "Photos") {
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    items(uiState.photoModels.values.toList()) { model ->
                                        Card(
                                            modifier = Modifier.size(120.dp),
                                            shape = RoundedCornerShape(8.dp),
                                            elevation = CardDefaults.cardElevation(8.dp),
                                        ) {
                                            Image(
                                                painter = rememberAsyncImagePainter(model = model),
                                                contentDescription = "Zone Photo",
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize(),
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        if (uiState.canUserManageZone) {
                            DetailCard(title = "Actions") {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(
                                        onClick = { onNavigateToUpdateZone(zone.id) },
                                        modifier = Modifier.fillMaxWidth(),
                                        enabled = !uiState.isLoading,
                                    ) {
                                        Text("Edit Zone")
                                    }
                                    LoadingOutlinedButton(
                                        onClick = { showDeleteDialog.value = true },
                                        modifier = Modifier.fillMaxWidth(),
                                        isLoading =
                                            uiState.actionState ==
                                                ZoneDetailsUiState.ActionState.DELETING,
                                        enabled = !uiState.isActionInProgress,
                                        colors =
                                            ButtonDefaults.outlinedButtonColors(
                                                contentColor = MaterialTheme.colorScheme.error,
                                            ),
                                        text = "Delete Zone",
                                        loadingIndicatorColor = MaterialTheme.colorScheme.error,
                                    )
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
