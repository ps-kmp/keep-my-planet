package pt.isel.keepmyplanet.ui.zone.details

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Report
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.rememberAsyncImagePainter
import kotlinx.coroutines.flow.collectLatest
import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.domain.zone.ZoneStatus
import pt.isel.keepmyplanet.navigation.rememberSavableScrollState
import pt.isel.keepmyplanet.ui.components.AppTopBar
import pt.isel.keepmyplanet.ui.components.ConfirmActionDialog
import pt.isel.keepmyplanet.ui.components.DetailCard
import pt.isel.keepmyplanet.ui.components.ErrorState
import pt.isel.keepmyplanet.ui.components.InfoRow
import pt.isel.keepmyplanet.ui.components.LoadingOutlinedButton
import pt.isel.keepmyplanet.ui.components.StatusBadge
import pt.isel.keepmyplanet.ui.components.ZoneDetailsSkeleton
import pt.isel.keepmyplanet.ui.components.getSeverityColorPair
import pt.isel.keepmyplanet.ui.components.getStatusColorPair
import pt.isel.keepmyplanet.ui.components.rememberPhotoPicker
import pt.isel.keepmyplanet.ui.zone.details.components.FullScreenPhotoViewer
import pt.isel.keepmyplanet.ui.zone.details.states.ZoneDetailsEvent
import pt.isel.keepmyplanet.ui.zone.details.states.ZoneDetailsUiState

@Composable
fun ZoneDetailsScreen(
    viewModel: ZoneDetailsViewModel,
    zoneId: Id,
    onNavigateToHome: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToCreateEvent: (zoneId: Id) -> Unit,
    onNavigateToEventDetails: (eventId: Id) -> Unit,
    onNavigateToUpdateZone: (zoneId: Id) -> Unit,
    onNavigateToMap: (latitude: Double, longitude: Double) -> Unit,
    onNavigateBack: () -> Unit,
    routeKey: String,
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberSavableScrollState(key = routeKey)
    val zone = uiState.zone
    val snackbarHostState = remember { SnackbarHostState() }

    val photoPicker =
        rememberPhotoPicker { imageData, filename ->
            viewModel.addAfterPhoto(imageData, filename)
        }

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

    uiState.selectedPhotoModel?.let { model ->
        FullScreenPhotoViewer(
            model = model,
            onDismiss = viewModel::onDismissPhotoViewer,
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
        topBar = {
            AppTopBar(
                title = "Zone Details",
                onNavigateBack = onNavigateBack,
                onNavigateToHome = onNavigateToHome,
            )
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentAlignment = Alignment.Center,
        ) {
            when {
                uiState.isLoading && zone == null -> ZoneDetailsSkeleton()
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
                                .verticalScroll(scrollState)
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
                            Column(
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                            ) {
                                uiState.reporter?.let { reporter ->
                                    InfoRow(
                                        icon = Icons.Default.Report,
                                        text = "Reported by ${reporter.name.value}",
                                    )
                                }

                                InfoRow(
                                    icon = Icons.Default.Radar,
                                    text = "Radius: ${zone.radius.value} meters",
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Flag,
                                        contentDescription = "Status",
                                        modifier = Modifier.padding(end = 16.dp),
                                        tint = MaterialTheme.colorScheme.primary,
                                    )
                                    val (statusBg, statusContent) = getStatusColorPair(zone.status)
                                    StatusBadge(
                                        text = zone.status.name.replace('_', ' '),
                                        backgroundColor = statusBg,
                                        contentColor = statusContent,
                                    )
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = "Severity",
                                        modifier = Modifier.padding(end = 16.dp),
                                        tint = MaterialTheme.colorScheme.primary,
                                    )
                                    val (severityBg, severityContent) =
                                        getSeverityColorPair(
                                            zone.zoneSeverity,
                                        )
                                    StatusBadge(
                                        text = zone.zoneSeverity.name,
                                        backgroundColor = severityBg,
                                        contentColor = severityContent,
                                        icon = Icons.Default.Warning,
                                    )
                                }
                                InfoRow(
                                    icon = Icons.Default.Map,
                                    text = "View on Map",
                                    isClickable = true,
                                    onClick = {
                                        onNavigateToMap(
                                            zone.location.latitude,
                                            zone.location.longitude,
                                        )
                                    },
                                )
                            }
                        }

                        if (uiState.beforePhotos.isNotEmpty()) {
                            DetailCard(title = "Before Cleanup") {
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    itemsIndexed(
                                        uiState.beforePhotos,
                                        key = { _, item -> item.first.value.toString() },
                                    ) { _, model ->
                                        Card(
                                            modifier =
                                                Modifier
                                                    .size(120.dp)
                                                    .clickable {
                                                        viewModel.onPhotoClicked(model.second)
                                                    },
                                            shape = RoundedCornerShape(8.dp),
                                            elevation = CardDefaults.cardElevation(8.dp),
                                        ) {
                                            Image(
                                                painter =
                                                    rememberAsyncImagePainter(
                                                        model = model.second,
                                                    ),
                                                contentDescription = "Zone Photo",
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize(),
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        if (uiState.afterPhotos.isNotEmpty()) {
                            DetailCard(title = "After Cleanup") {
                                Box(
                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                MaterialTheme.colorScheme.primary.copy(
                                                    alpha = 0.05f,
                                                ),
                                            ).padding(vertical = 8.dp),
                                ) {
                                    LazyRow(
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp),
                                    ) {
                                        itemsIndexed(
                                            uiState.afterPhotos,
                                            key = { _, item -> item.first.value.toString() },
                                        ) { _, model ->
                                            Card(
                                                modifier =
                                                    Modifier
                                                        .size(120.dp)
                                                        .clickable {
                                                            viewModel.onPhotoClicked(
                                                                model.second,
                                                            )
                                                        },
                                                shape = RoundedCornerShape(8.dp),
                                                elevation = CardDefaults.cardElevation(8.dp),
                                            ) {
                                                Image(
                                                    painter =
                                                        rememberAsyncImagePainter(
                                                            model = model.second,
                                                        ),
                                                    contentDescription = "Zone Photo",
                                                    contentScale = ContentScale.Crop,
                                                    modifier = Modifier.fillMaxSize(),
                                                )
                                            }
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
                                    if (zone.status == ZoneStatus.CLEANED) {
                                        OutlinedButton(
                                            onClick = photoPicker,
                                            modifier = Modifier.fillMaxWidth(),
                                            enabled = !uiState.isActionInProgress,
                                        ) {
                                            if (uiState.actionState ==
                                                ZoneDetailsUiState.ActionState.ADDINGPHOTO
                                            ) {
                                                CircularProgressIndicator(
                                                    modifier = Modifier.size(24.dp),
                                                )
                                            } else {
                                                Icon(
                                                    Icons.Default.AddAPhoto,
                                                    contentDescription = null,
                                                    modifier = Modifier.padding(end = 8.dp),
                                                )
                                                Text("Add 'After' Photo")
                                            }
                                        }
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
                                    ) { Text("Delete Zone") }
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
                        } else {
                            when {
                                uiState.isGuest ->
                                    Button(
                                        onClick = onNavigateToLogin,
                                        modifier = Modifier.fillMaxWidth(),
                                    ) {
                                        Text("Login to Create Event")
                                    }

                                zone.status == ZoneStatus.REPORTED ->
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
                }

                else -> {
                    Text("Could not load zone details.")
                }
            }
        }
    }
}
