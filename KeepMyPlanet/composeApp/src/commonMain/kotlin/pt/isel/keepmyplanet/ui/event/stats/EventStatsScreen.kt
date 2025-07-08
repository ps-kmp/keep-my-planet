package pt.isel.keepmyplanet.ui.event.stats

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.collectLatest
import pt.isel.keepmyplanet.ui.components.AppTopBar
import pt.isel.keepmyplanet.ui.components.ErrorState
import pt.isel.keepmyplanet.ui.components.FullScreenLoading
import pt.isel.keepmyplanet.ui.event.stats.components.EventStatsSummaryCard
import pt.isel.keepmyplanet.ui.event.stats.states.EventStatsEvent

@Composable
fun EventStatsScreen(
    viewModel: EventStatsViewModel,
    onNavigateToHome: () -> Unit,
    onNavigateBack: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel.events) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is EventStatsEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(
                        message = event.message,
                        duration = SnackbarDuration.Short,
                    )
                }
            }
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Event Statistics",
                onNavigateBack = onNavigateBack,
                onNavigateToHome = onNavigateToHome,
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { padding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
        ) {
            if (uiState.isLoading) {
                FullScreenLoading()
            } else if (uiState.error != null) {
                ErrorState(
                    message = uiState.error!!,
                    onRetry = { viewModel.loadStats() },
                )
            } else if (uiState.stats != null) {
                EventStatsSummaryCard(stats = uiState.stats!!)
            } else {
                ErrorState(
                    message = "Could not load event statistics.",
                    onRetry = { viewModel.loadStats() },
                )
            }
        }
    }
}
