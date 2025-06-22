package pt.isel.keepmyplanet.ui.event.attendance

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import kotlinx.coroutines.flow.collectLatest
import pt.isel.keepmyplanet.ui.components.AppTopBar
import pt.isel.keepmyplanet.ui.components.ErrorState
import pt.isel.keepmyplanet.ui.components.FullScreenLoading
import pt.isel.keepmyplanet.ui.components.QrCodeScannerView
import pt.isel.keepmyplanet.ui.event.attendance.model.ManageAttendanceEvent

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ManageAttendanceScreen(
    viewModel: ManageAttendanceViewModel,
    onNavigateBack: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel.events) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is ManageAttendanceEvent.ShowSnackbar -> {
                    snackbarHostState.currentSnackbarData?.dismiss()
                    snackbarHostState.showSnackbar(event.message)
                }
            }
        }
    }

    Scaffold(
        topBar = { AppTopBar(title = "Manage Attendance", onNavigateBack = onNavigateBack) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                uiState.isLoading -> FullScreenLoading()
                uiState.error != null ->
                    ErrorState(
                        message = uiState.error!!,
                        onRetry = viewModel::loadInitialData,
                    )

                else -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                            QrCodeScannerView(
                                modifier = Modifier.fillMaxSize(),
                                onQrCodeScanned = viewModel::onQrCodeScanned,
                            )
                        }

                        // TODO: List of "Attendees" and "Remaining"
                        Text("Attendees: ${uiState.attendees.size} / ${uiState.participants.size}")
                    }
                }
            }
        }
    }
}
