package pt.isel.keepmyplanet.ui.zone.update

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
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
import kotlinx.coroutines.flow.collectLatest
import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.domain.zone.ZoneSeverity
import pt.isel.keepmyplanet.ui.components.AppTopBar
import pt.isel.keepmyplanet.ui.components.ErrorState
import pt.isel.keepmyplanet.ui.components.FormField
import pt.isel.keepmyplanet.ui.components.LoadingButton
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
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(16.dp),
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

                    Spacer(modifier = Modifier.weight(1f))

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
