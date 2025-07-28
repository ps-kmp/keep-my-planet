package pt.isel.keepmyplanet.ui.attendance

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.collectLatest
import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.navigation.rememberSavableLazyListState
import pt.isel.keepmyplanet.ui.attendance.components.AttendanceListItem
import pt.isel.keepmyplanet.ui.attendance.states.ManageAttendanceEvent
import pt.isel.keepmyplanet.ui.attendance.states.ManageAttendanceTab
import pt.isel.keepmyplanet.ui.components.AppTopBar
import pt.isel.keepmyplanet.ui.components.EmptyState
import pt.isel.keepmyplanet.ui.components.ErrorState
import pt.isel.keepmyplanet.ui.components.FullScreenLoading
import pt.isel.keepmyplanet.ui.components.QrCodeScannerView

@Composable
fun ManageAttendanceScreen(
    viewModel: ManageAttendanceViewModel,
    eventId: Id,
    onNavigateToHome: () -> Unit,
    onNavigateBack: () -> Unit,
    routeKey: String,
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val attendeesListState = rememberSavableLazyListState("$routeKey-attendees")
    val remainingListState = rememberSavableLazyListState("$routeKey-remaining")

    LaunchedEffect(viewModel.events) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is ManageAttendanceEvent.ShowSnackbar ->
                    snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Manage Attendance",
                onNavigateBack = onNavigateBack,
                onNavigateToHome = onNavigateToHome,
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                uiState.isLoading -> FullScreenLoading()
                uiState.error != null ->
                    ErrorState(
                        message = uiState.error!!,
                        onRetry = { viewModel.loadInitialData(eventId) },
                    )

                else -> {
                    val tabs = ManageAttendanceTab.entries
                    TabRow(selectedTabIndex = tabs.indexOf(uiState.selectedTab)) {
                        tabs.forEach { tab ->
                            val text =
                                when (tab) {
                                    ManageAttendanceTab.SCANNER -> "Scanner"
                                    ManageAttendanceTab.ATTENDEES ->
                                        "Attendees (${uiState.attendees.size})"

                                    ManageAttendanceTab.REMAINING ->
                                        "Remaining (${uiState.remainingParticipants.size})"
                                }
                            Tab(
                                selected = uiState.selectedTab == tab,
                                onClick = { viewModel.onTabSelected(tab) },
                                text = { Text(text) },
                            )
                        }
                    }

                    Crossfade(
                        targetState = uiState.selectedTab,
                        label = "TabContent",
                    ) { selectedTab ->
                        when (selectedTab) {
                            ManageAttendanceTab.SCANNER -> {
                                Box(modifier = Modifier.fillMaxSize()) {
                                    QrCodeScannerView(
                                        modifier = Modifier.fillMaxSize(),
                                        onQrCodeScanned = {
                                            if (!uiState.isCheckingIn) {
                                                viewModel.onQrCodeScanned(it)
                                            }
                                        },
                                    )
                                    if (uiState.isCheckingIn) {
                                        Box(
                                            modifier =
                                                Modifier
                                                    .fillMaxSize()
                                                    .background(Color.Black.copy(alpha = 0.5f)),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                            ) {
                                                CircularProgressIndicator(color = Color.White)
                                                Spacer(Modifier.height(16.dp))
                                                Text(
                                                    "Processing...",
                                                    color = Color.White,
                                                    style = MaterialTheme.typography.bodyLarge,
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            ManageAttendanceTab.ATTENDEES -> {
                                if (uiState.attendees.isEmpty()) {
                                    EmptyState(message = "No one has checked in yet.")
                                } else {
                                    LazyColumn(
                                        state = attendeesListState,
                                        modifier = Modifier.fillMaxSize(),
                                    ) {
                                        items(
                                            uiState.attendees,
                                            key = { it.id.value.toString() },
                                        ) { user ->
                                            AttendanceListItem(user, false, false) {}
                                            HorizontalDivider()
                                        }
                                    }
                                }
                            }

                            ManageAttendanceTab.REMAINING -> {
                                if (uiState.remainingParticipants.isEmpty()) {
                                    EmptyState(message = "Everyone has checked in!")
                                } else {
                                    LazyColumn(
                                        state = remainingListState,
                                        modifier = Modifier.fillMaxSize(),
                                    ) {
                                        items(
                                            uiState.remainingParticipants,
                                            key = { it.id.value.toString() },
                                        ) { user ->
                                            AttendanceListItem(
                                                user = user,
                                                isProcessing = uiState.isCheckingIn,
                                                showCheckInButton = true,
                                                onCheckInClick = {
                                                    viewModel.manualCheckInUser(user.id)
                                                },
                                            )
                                            HorizontalDivider()
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
