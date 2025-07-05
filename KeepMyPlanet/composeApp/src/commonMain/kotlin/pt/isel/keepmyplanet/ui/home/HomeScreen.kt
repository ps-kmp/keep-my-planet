package pt.isel.keepmyplanet.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.collectLatest
import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.ui.base.koinViewModel
import pt.isel.keepmyplanet.ui.components.AppTopBar
import pt.isel.keepmyplanet.ui.components.rememberLocationProvider
import pt.isel.keepmyplanet.ui.home.components.DashboardItem
import pt.isel.keepmyplanet.ui.home.components.EventSummaryCard
import pt.isel.keepmyplanet.ui.home.components.ZoneSummaryCard
import pt.isel.keepmyplanet.ui.home.states.HomeEvent

@Composable
fun HomeScreen(
    onNavigateToEventList: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToMap: () -> Unit,
    onNavigateToAbout: () -> Unit,
    onNavigateToEventDetails: (Id) -> Unit,
    onNavigateToZoneDetails: (Id) -> Unit,
    onLogout: () -> Unit,
) {
    val viewModel: HomeViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsState()
    val user = uiState.user

    val locationProvider =
        rememberLocationProvider(
            onLocationUpdated = { lat, lon ->
                viewModel.onLocationUpdateReceived()
                viewModel.onLocationAvailable(lat, lon)
            },
            onLocationError = {
                viewModel.onLocationError()
            },
        )

    LaunchedEffect(Unit) {
        viewModel.requestLocationUpdate()
    }

    LaunchedEffect(viewModel.events) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is HomeEvent.RequestLocation -> locationProvider.requestLocationUpdate()
                is HomeEvent.ShowSnackbar -> {}
            }
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "KeepMyPlanet",
                actions = {
                    IconButton(onClick = onNavigateToAbout) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "About Page",
                        )
                    }
                    IconButton(onClick = onNavigateToProfile) {
                        Icon(Icons.Default.Person, contentDescription = "Profile")
                    }
                },
            )
        },
    ) { paddingValues ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(paddingValues)
                    .padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            if (user != null) {
                Text(
                    text = "Welcome, ${user.name.value}!",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )

                DashboardSection(
                    title = "Your Upcoming Events",
                    onViewAll = onNavigateToEventList,
                ) {
                    if (uiState.isLoading && uiState.upcomingEvents.isEmpty()) {
                        CircularProgressIndicator()
                    } else if (uiState.upcomingEvents.isNotEmpty()) {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            items(uiState.upcomingEvents) { event ->
                                EventSummaryCard(
                                    event = event,
                                    onClick = { onNavigateToEventDetails(event.id) },
                                )
                            }
                        }
                    } else {
                        Text("No upcoming events.", modifier = Modifier.padding(horizontal = 16.dp))
                    }
                }

                DashboardSection(
                    title = "Nearby Polluted Zones",
                    onViewAll = onNavigateToMap,
                ) {
                    if (uiState.isLocating) {
                        CircularProgressIndicator()
                    } else if (uiState.nearbyZones.isNotEmpty()) {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            items(uiState.nearbyZones) { zone ->
                                ZoneSummaryCard(
                                    zone = zone,
                                    onClick = { onNavigateToZoneDetails(zone.id) },
                                )
                            }
                        }
                    } else {
                        if (uiState.locationError) {
                            Text(
                                "Could not retrieve location to find nearby zones.",
                                modifier = Modifier.padding(horizontal = 16.dp),
                            )
                        } else {
                            Text(
                                "No polluted zones found nearby. Great!",
                                modifier = Modifier.padding(horizontal = 16.dp),
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    DashboardItem(
                        icon = Icons.Default.Map,
                        title = "Interactive Map",
                        description = "View all polluted zones or report a new one.",
                        onClick = onNavigateToMap,
                    )

                    DashboardItem(
                        icon = Icons.AutoMirrored.Filled.ListAlt,
                        title = "All Cleanup Events",
                        description = "Find and join events organized by the community.",
                        onClick = onNavigateToEventList,
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                OutlinedButton(
                    onClick = onLogout,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    colors =
                        ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error,
                        ),
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ExitToApp,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 8.dp),
                    )
                    Text("Logout")
                }
            } else if (uiState.isLoading) {
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
private fun DashboardSection(
    title: String,
    onViewAll: () -> Unit,
    content: @Composable () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            TextButton(onClick = onViewAll) {
                Text("View All")
            }
        }
        content()
    }
}
