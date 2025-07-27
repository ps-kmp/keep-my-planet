package pt.isel.keepmyplanet.ui.zone.update

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.rememberAsyncImagePainter
import kotlinx.coroutines.flow.collectLatest
import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.domain.zone.ZoneSeverity
import pt.isel.keepmyplanet.domain.zone.ZoneStatus
import pt.isel.keepmyplanet.ui.components.AppTopBar
import pt.isel.keepmyplanet.ui.components.ErrorState
import pt.isel.keepmyplanet.ui.components.FormField
import pt.isel.keepmyplanet.ui.components.LoadingButton
import pt.isel.keepmyplanet.ui.components.rememberPhotoPicker
import pt.isel.keepmyplanet.ui.zone.update.states.UpdateZoneEvent

@Composable
fun UpdateZoneScreen(
    viewModel: UpdateZoneViewModel,
    onNavigateToHome: () -> Unit,
    zoneId: Id,
    onNavigateBack: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    val beforePhotoPicker =
        rememberPhotoPicker { imageData, filename ->
            viewModel.addPhoto(imageData, filename, "BEFORE")
        }
    val afterPhotoPicker =
        rememberPhotoPicker { imageData, filename ->
            viewModel.addPhoto(imageData, filename, "AFTER")
        }
    LaunchedEffect(zoneId) {
        viewModel.loadZone(zoneId)
    }

    LaunchedEffect(viewModel.events) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is UpdateZoneEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
                is UpdateZoneEvent.UpdateSuccessful -> onNavigateBack()
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            AppTopBar(
                "Edit Zone",
                onNavigateToHome = onNavigateToHome,
                onNavigateBack = onNavigateBack,
            )
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentAlignment = Alignment.Center,
        ) {
            if (uiState.error != null && uiState.zone == null) {
                ErrorState(uiState.error!!) { viewModel.loadZone(zoneId) }
            } else {
                Column(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                ) {
                    Column(
                        modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        FormField(
                            value = uiState.description,
                            onValueChange = viewModel::onDescriptionChange,
                            label = "Description",
                            minLines = 4,
                            enabled = uiState.isFormEnabled,
                            errorText = uiState.descriptionError,
                        )

                        Text("Severity:", style = MaterialTheme.typography.titleMedium)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            ZoneSeverity.entries
                                .filter { it != ZoneSeverity.UNKNOWN }
                                .forEach { severity ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f),
                                    ) {
                                        RadioButton(
                                            selected = uiState.severity == severity,
                                            onClick = { viewModel.onSeverityChange(severity) },
                                            enabled = uiState.isFormEnabled,
                                        )
                                        Text(
                                            text = severity.name,
                                            modifier = Modifier.padding(start = 4.dp),
                                        )
                                    }
                                }
                        }

                        Text("Before Cleanup Photos", style = MaterialTheme.typography.titleMedium)
                        if (uiState.beforePhotos.isNotEmpty()) {
                            LazyRow(
                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                items(
                                    uiState.beforePhotos,
                                    key = { it.first.value.toString() },
                                ) { (photoId, model) ->
                                    ImageThumbnail(
                                        model = model,
                                        onRemove = { viewModel.onRemovePhoto(photoId) },
                                        enabled = uiState.isFormEnabled,
                                    )
                                }
                            }
                        }
                        OutlinedButton(
                            onClick = beforePhotoPicker,
                            enabled =
                                uiState.isFormEnabled &&
                                    uiState.beforePhotos.size < 5 &&
                                    uiState.zone?.status == ZoneStatus.REPORTED,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Icon(
                                Icons.Default.AddAPhoto,
                                contentDescription = "Add Before Photo",
                                modifier = Modifier.padding(end = 8.dp),
                            )
                            Text("Add 'Before' Photo (${uiState.beforePhotos.size}/5)")
                        }

                        Spacer(Modifier.height(16.dp))

                        Text("After Cleanup Photos", style = MaterialTheme.typography.titleMedium)
                        if (uiState.afterPhotos.isNotEmpty()) {
                            LazyRow(
                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                items(
                                    uiState.afterPhotos,
                                    key = { it.first.value.toString() },
                                ) { (photoId, model) ->
                                    ImageThumbnail(
                                        model = model,
                                        onRemove = { viewModel.onRemovePhoto(photoId) },
                                        enabled = uiState.isFormEnabled,
                                    )
                                }
                            }
                        }
                        OutlinedButton(
                            onClick = afterPhotoPicker,
                            enabled =
                                uiState.isFormEnabled &&
                                    uiState.afterPhotos.size < 5 &&
                                    uiState.zone?.status == ZoneStatus.CLEANED,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Icon(
                                Icons.Default.AddAPhoto,
                                contentDescription = "Add After Photo",
                                modifier = Modifier.padding(end = 8.dp),
                            )
                            Text("Add 'After' Photo (${uiState.afterPhotos.size}/5)")
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    LoadingButton(
                        onClick = viewModel::submitUpdate,
                        isLoading = uiState.isUpdating,
                        enabled = uiState.isFormEnabled,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Save Changes")
                    }
                }
            }
        }
    }
}

@Composable
private fun ImageThumbnail(
    model: Any,
    onRemove: () -> Unit,
    enabled: Boolean,
) {
    Card(
        modifier = Modifier.size(100.dp),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(4.dp),
    ) {
        Box {
            Image(
                painter = rememberAsyncImagePainter(model = model),
                contentDescription = "Zone photo",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
            IconButton(
                onClick = onRemove,
                enabled = enabled,
                modifier =
                    Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                        .size(24.dp),
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Remove photo",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}
