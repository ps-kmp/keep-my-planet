package pt.isel.keepmyplanet.ui.attendance

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
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
import kotlinx.coroutines.flow.collectLatest
import pt.isel.keepmyplanet.ui.attendance.states.ManageAttendanceEvent
import pt.isel.keepmyplanet.ui.components.AppTopBar
import pt.isel.keepmyplanet.ui.components.ErrorState
import pt.isel.keepmyplanet.ui.components.FullScreenLoading
import pt.isel.keepmyplanet.ui.components.QrCodeScannerView

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
                            if (uiState.isCheckingIn) {
                                Box(
                                    modifier =
                                        Modifier
                                            .fillMaxSize()
                                            .background(Color.Black.copy(alpha = 0.5f)),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    CircularProgressIndicator(color = Color.White)
                                }
                            }
                        }

                        // TODO: List of "Attendees" and "Remaining"
                        Text("Attendees: ${uiState.attendees.size} / ${uiState.participants.size}")
                    }
                }
            }
        }
    }
}
