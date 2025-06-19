package pt.isel.keepmyplanet.ui.event.attendance

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.ui.components.QrCodeScannerView
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import pt.isel.keepmyplanet.ui.components.AppTopBar

@Composable
fun ManageAttendanceScreen(
    viewModel: ManageAttendanceViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.checkInStatusMessage) {
        uiState.checkInStatusMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearStatusMessage()
        }
    }

    Scaffold(
        topBar = { AppTopBar(title = "Manage Attendance", onNavigateBack = onNavigateBack) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            Box(modifier = Modifier.fillMaxWidth().fillMaxHeight(0.5f)) {
                QrCodeScannerView(
                    modifier = Modifier.fillMaxSize(),
                    onQrCodeScanned = { qrData ->
                        val userId = try {
                            Id(qrData.toUInt())
                        } catch (e: Exception) {
                            null
                        }

                        if (userId != null) {
                            viewModel.checkInUser(userId)
                        } else {

                        }
                    }
                )
            }

            // TODO: List of "Attendees" and "Remaining"
            Text("Attendees: ${uiState.attendees.size} / ${uiState.participants.size}")
        }
    }
}
