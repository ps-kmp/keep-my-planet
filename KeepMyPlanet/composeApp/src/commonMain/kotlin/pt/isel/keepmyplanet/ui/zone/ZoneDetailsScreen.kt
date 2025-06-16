package pt.isel.keepmyplanet.ui.zone

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pt.isel.keepmyplanet.ui.components.AppTopBar
import pt.isel.keepmyplanet.ui.components.FullScreenLoading

@Suppress("ktlint:standard:function-naming")
@Composable
fun ZoneDetailsScreen(
    viewModel: ZoneViewModel,
    zoneId: UInt,
    onNavigateBack: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val zone = uiState.zoneDetails

    LaunchedEffect(zoneId) {
        viewModel.loadZoneDetails(zoneId)
    }

    Scaffold(
        topBar = { AppTopBar(title = "Zone Details", onNavigateBack = onNavigateBack) },
    ) { paddingValues ->
        Box(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentAlignment = Alignment.Center,
        ) {
            when {
                uiState.isLoading -> FullScreenLoading()
                zone != null -> {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Text("Zone #${zone.id.value}", style = MaterialTheme.typography.h5)
                        Text("Description:", style = MaterialTheme.typography.subtitle1)
                        Text(zone.description.value, style = MaterialTheme.typography.body1)
                        Text(
                            "Severity: ${zone.zoneSeverity.name}",
                            style = MaterialTheme.typography.body2,
                        )
                        Text("Status: ${zone.status.name}", style = MaterialTheme.typography.body2)
                        Text(
                            "Reported by: User ${zone.reporterId.value}",
                            style = MaterialTheme.typography.caption,
                        )
                        if (zone.eventId != null) {
                            Text(
                                "Event ID: ${zone.eventId?.value}",
                                style = MaterialTheme.typography.caption,
                            )
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
