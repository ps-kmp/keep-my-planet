package pt.isel.keepmyplanet.ui.report

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material3.Icon
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
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt
import kotlinx.coroutines.flow.collectLatest
import pt.isel.keepmyplanet.domain.zone.ZoneSeverity
import pt.isel.keepmyplanet.ui.components.AppTopBar
import pt.isel.keepmyplanet.ui.components.FormField
import pt.isel.keepmyplanet.ui.components.LoadingButton
import pt.isel.keepmyplanet.ui.components.rememberPhotoPicker
import pt.isel.keepmyplanet.ui.report.components.ImageThumbnail
import pt.isel.keepmyplanet.ui.report.states.ReportZoneEvent
import pt.isel.keepmyplanet.ui.report.states.ReportZoneUiState

@Composable
fun ReportZoneScreen(
    viewModel: ReportZoneViewModel,
    onNavigateToHome: () -> Unit,
    latitude: Double,
    longitude: Double,
    radius: Double,
    onNavigateBack: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val isActionInProgress = uiState.actionState is ReportZoneUiState.ActionState.Submitting

    LaunchedEffect(latitude, longitude, radius) {
        viewModel.prepareReportForm(latitude, longitude, radius)
    }

    val launchPhotoPicker =
        rememberPhotoPicker { imageData, filename ->
            viewModel.onPhotoSelected(imageData, filename)
        }

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is ReportZoneEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
                is ReportZoneEvent.ReportSuccessful -> onNavigateBack()
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            AppTopBar(
                title = "Report Polluted Zone",
                onNavigateBack = onNavigateBack,
                onNavigateToHome = onNavigateToHome,
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
        ) {
            Column(
                modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    "Reporting for location:\nLat: ${uiState.latitude}, Lon: ${uiState.longitude}",
                    style = MaterialTheme.typography.bodySmall,
                )
                Text(
                    "Radius: ${uiState.radius.roundToInt()} meters",
                    style = MaterialTheme.typography.bodySmall,
                )

                FormField(
                    value = uiState.description,
                    onValueChange = viewModel::onReportDescriptionChange,
                    label = "Description of the issue",
                    minLines = 4,
                    enabled = !isActionInProgress,
                    errorText = uiState.descriptionError,
                )

                Text("Severity:", style = MaterialTheme.typography.bodySmall)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    ZoneSeverity.entries.filter { it != ZoneSeverity.UNKNOWN }.forEach { severity ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f),
                        ) {
                            RadioButton(
                                selected = uiState.severity == severity,
                                onClick = { viewModel.onReportSeverityChange(severity) },
                                enabled = !isActionInProgress,
                            )
                            Text(
                                text = severity.name,
                                modifier = Modifier.padding(start = 4.dp),
                            )
                        }
                    }
                }

                OutlinedButton(
                    onClick = launchPhotoPicker,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isActionInProgress && uiState.photos.size < 5,
                ) {
                    Icon(
                        Icons.Default.AddAPhoto,
                        contentDescription = "Add Photo",
                        modifier = Modifier.padding(end = 8.dp),
                    )
                    Text("Add Photo (${uiState.photos.size}/5)")
                }

                if (uiState.photos.isNotEmpty()) {
                    LazyRow(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(uiState.photos, key = { it.hashCode() }) { image ->
                            ImageThumbnail(
                                image,
                                { viewModel.onRemovePhoto(image) },
                                !isActionInProgress,
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            LoadingButton(
                onClick = viewModel::submitZoneReport,
                isLoading = isActionInProgress,
                enabled = uiState.canSubmit,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Submit Report")
            }
        }
    }
}
