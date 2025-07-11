package pt.isel.keepmyplanet.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.collectLatest
import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.ui.base.koinViewModel
import pt.isel.keepmyplanet.ui.components.AppTopBar
import pt.isel.keepmyplanet.ui.components.EventSummaryCardSkeleton
import pt.isel.keepmyplanet.ui.components.rememberLocationProvider
import pt.isel.keepmyplanet.ui.home.components.ActionableEventCard
import pt.isel.keepmyplanet.ui.home.components.DashboardItem
import pt.isel.keepmyplanet.ui.home.components.EventSummaryCard
import pt.isel.keepmyplanet.ui.home.components.OnboardingDialog
import pt.isel.keepmyplanet.ui.home.components.ZoneSummaryCard
import pt.isel.keepmyplanet.ui.home.states.HomeEvent
import pt.isel.keepmyplanet.ui.home.states.HomeUiState
import pt.isel.keepmyplanet.ui.theme.primaryLight

@Composable
fun HomeScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToEventList: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToMap: () -> Unit,
    onNavigateToEventDetails: (Id) -> Unit,
    onNavigateToZoneDetails: (Id) -> Unit,
    onLogout: () -> Unit,
    onNavigateToUserManagement: () -> Unit,
    onNavigateToAbout: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToRegister: () -> Unit,
) {
    val viewModel: HomeViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val user = uiState.user

    if (uiState.showOnboarding) {
        OnboardingDialog(onDismiss = viewModel::completeOnboarding)
    }

    val locationProvider =
        rememberLocationProvider(
            onLocationUpdated = { lat, lon ->
                viewModel.onLocationAvailable(lat, lon)
            },
            onLocationError = {
                viewModel.onLocationError()
            },
        )

    LaunchedEffect(viewModel.events) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is HomeEvent.RequestLocation -> {
                    if (locationProvider.isPermissionGranted) {
                        locationProvider.requestLocationUpdate()
                    } else {
                        locationProvider.requestPermission()
                    }
                }

                is HomeEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(event.message)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "KeepMyPlanet",
                onNavigateToHome = onNavigateToHome,
                actions = {
                    if (user != null) {
                        IconButton(onClick = onNavigateToAbout) {
                            Icon(Icons.Default.Info, "About")
                        }
                        IconButton(onClick = onNavigateToProfile) {
                            Icon(Icons.Default.Person, "Profile")
                        }
                    } else {
                        TextButton(onClick = onNavigateToLogin) { Text("Login") }
                        TextButton(onClick = onNavigateToRegister) { Text("Register") }
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { paddingValues ->
        if (uiState.isLoading && user == null) {
            Column(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                CircularProgressIndicator()
            }
        } else if (user != null) {
            UserDashboard(
                uiState = uiState,
                paddingValues = paddingValues,
                onNavigateToEventList = onNavigateToEventList,
                onNavigateToEventDetails = onNavigateToEventDetails,
                onNavigateToZoneDetails = onNavigateToZoneDetails,
                onNavigateToUserManagement = onNavigateToUserManagement,
                onLogout = onLogout,
                onFindNearbyZones = viewModel::onFindNearbyZonesRequested,
                onNavigateToMap = onNavigateToMap,
            )
        } else {
            GuestHomeScreen(
                paddingValues = paddingValues,
                onNavigateToMap = onNavigateToMap,
                onNavigateToEventList = onNavigateToEventList,
                onNavigateToLogin = onNavigateToLogin,
            )
        }
    }
}

@Composable
private fun UserDashboard(
    uiState: HomeUiState,
    paddingValues: PaddingValues,
    onNavigateToEventList: () -> Unit,
    onNavigateToEventDetails: (Id) -> Unit,
    onNavigateToZoneDetails: (Id) -> Unit,
    onNavigateToUserManagement: () -> Unit,
    onLogout: () -> Unit,
    onFindNearbyZones: () -> Unit,
    onNavigateToMap: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(paddingValues),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        item {
            uiState.user?.let { user ->
                Text(
                    text = "Welcome, ${user.name.value}!",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }
        }

        if (uiState.pendingActions.isNotEmpty()) {
            item {
                DashboardSection("Pending Actions") {
                    uiState.pendingActions.forEach { event ->
                        ActionableEventCard(
                            event = event,
                            onClick = { onNavigateToEventDetails(event.id) },
                        )
                    }
                }
            }
        }

        item {
            DashboardSection(
                title = "Your Upcoming Events",
                onActionClick = onNavigateToEventList,
                actionText = "View All",
            ) {
                when {
                    uiState.isLoading -> {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp),
                        ) {
                            items(3) { EventSummaryCardSkeleton() }
                        }
                    }

                    uiState.upcomingEvents.isNotEmpty() -> {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp),
                        ) {
                            items(uiState.upcomingEvents) { event ->
                                EventSummaryCard(
                                    event = event,
                                    onClick = { onNavigateToEventDetails(event.id) },
                                )
                            }
                        }
                    }

                    else -> {
                        Text(
                            "You have no upcoming events. Why not join one?",
                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp),
                        )
                    }
                }
            }
        }

        item {
            DashboardSection(
                title = "Find Nearby Polluted Zones",
                onActionClick = onNavigateToMap,
                actionText = "Explore on Map",
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    when {
                        uiState.isFindingZones -> {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(Modifier.size(24.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Finding zones near you...")
                            }
                        }

                        uiState.zonesFound == true && uiState.nearbyZones.isNotEmpty() -> {
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                items(uiState.nearbyZones) { zone ->
                                    ZoneSummaryCard(
                                        zone = zone,
                                        onClick = { onNavigateToZoneDetails(zone.id) },
                                    )
                                }
                            }
                        }

                        uiState.zonesFound == false -> {
                            Text(
                                "No polluted zones found nearby. Great!",
                                modifier = Modifier.padding(vertical = 8.dp),
                            )
                        }

                        else -> {
                            Button(onClick = onFindNearbyZones) {
                                Text("Find Nearby Zones")
                            }
                        }
                    }
                }
            }
        }

        item {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                if (uiState.isUserAdmin) {
                    DashboardItem(
                        icon = Icons.Default.AdminPanelSettings,
                        title = "User Management",
                        description = "View all users and manage their roles.",
                        onClick = onNavigateToUserManagement,
                    )
                }
                OutlinedButton(
                    onClick = onLogout,
                    modifier = Modifier.fillMaxWidth(),
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
            }
        }
    }
}

@Composable
private fun GuestHomeScreen(
    paddingValues: PaddingValues,
    onNavigateToMap: () -> Unit,
    onNavigateToEventList: () -> Unit,
    onNavigateToLogin: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(paddingValues),
        contentPadding = PaddingValues(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = "Welcome to KeepMyPlanet",
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text =
                        "Join a global community dedicated to cleaning our world, " +
                            "one zone at a time.",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        item {
            DashboardItem(
                icon = Icons.Default.Map,
                title = "Explore the Map",
                description = "Discover polluted zones reported by the community.",
                onClick = onNavigateToMap,
            )
        }
        item {
            DashboardItem(
                icon = Icons.AutoMirrored.Filled.ListAlt,
                title = "Find a Cleanup Event",
                description = "Browse and see details about upcoming events near you.",
                onClick = onNavigateToEventList,
            )
        }

        item {
            Column(modifier = Modifier.padding(top = 32.dp)) {
                Button(
                    onClick = onNavigateToLogin,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = primaryLight),
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Login,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 8.dp),
                    )
                    Text("Login or Register to Participate")
                }
            }
        }
    }
}

@Composable
private fun DashboardSection(
    title: String,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
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
            if (actionText != null && onActionClick != null) {
                TextButton(onClick = onActionClick) {
                    Text(actionText)
                }
            }
        }
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(0.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        ) {
            Column(
                modifier = Modifier,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                content = content,
            )
        }
    }
}
