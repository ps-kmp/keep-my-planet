package pt.isel.keepmyplanet.ui.zone.report

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.RadioButton
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.collectLatest
import pt.isel.keepmyplanet.domain.zone.ZoneSeverity
import pt.isel.keepmyplanet.ui.components.AppTopBar
import pt.isel.keepmyplanet.ui.components.FormField
import pt.isel.keepmyplanet.ui.components.LoadingButton
import pt.isel.keepmyplanet.ui.zone.report.model.ReportZoneEvent
import pt.isel.keepmyplanet.ui.zone.report.model.ReportZoneUiState

@Composable
fun ReportZoneScreen(
    viewModel: ReportZoneViewModel,
    latitude: Double,
    longitude: Double,
    onNavigateBack: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val formState = uiState.form
    val snackbarHostState = remember { SnackbarHostState() }
    val isActionInProgress = uiState.actionState is ReportZoneUiState.ActionState.Submitting

    LaunchedEffect(latitude, longitude) {
        viewModel.prepareReportForm(latitude, longitude)
    }

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is ReportZoneEvent.ShowSnackbar ->
                    snackbarHostState.showSnackbar(
                        event.message,
                    )
                is ReportZoneEvent.ReportSuccessful -> onNavigateBack()
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = { AppTopBar(title = "Report Polluted Zone", onNavigateBack = onNavigateBack) },
    ) { paddingValues ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                "Reporting for location:\nLat: ${formState.latitude}, Lon: ${formState.longitude}",
                style = MaterialTheme.typography.caption,
            )

            FormField(
                value = formState.description,
                onValueChange = viewModel::onReportDescriptionChange,
                label = "Description of the issue",
                minLines = 4,
                enabled = !isActionInProgress,
                errorText = formState.descriptionError,
            )

            Text("Severity:", style = MaterialTheme.typography.subtitle1)
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
                            selected = formState.severity == severity,
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

            Spacer(modifier = Modifier.weight(1f))

            LoadingButton(
                onClick = viewModel::submitZoneReport,
                isLoading = isActionInProgress,
                enabled = uiState.canSubmit,
                text = "Submit Report",
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
