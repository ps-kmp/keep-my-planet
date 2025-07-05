package pt.isel.keepmyplanet.ui.map

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlin.math.pow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import ovh.plrapps.mapcompose.api.addCallout
import ovh.plrapps.mapcompose.api.centroidX
import ovh.plrapps.mapcompose.api.centroidY
import ovh.plrapps.mapcompose.api.maxScale
import ovh.plrapps.mapcompose.api.minScale
import ovh.plrapps.mapcompose.api.removeCallout
import ovh.plrapps.mapcompose.api.scale
import ovh.plrapps.mapcompose.api.scroll
import ovh.plrapps.mapcompose.api.setScroll
import ovh.plrapps.mapcompose.ui.MapUI
import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.ui.components.AppTopBar
import pt.isel.keepmyplanet.ui.components.ErrorState
import pt.isel.keepmyplanet.ui.components.FullScreenLoading
import pt.isel.keepmyplanet.ui.components.StatusBadge
import pt.isel.keepmyplanet.ui.components.getSeverityColor
import pt.isel.keepmyplanet.ui.components.rememberLocationProvider
import pt.isel.keepmyplanet.ui.map.components.MapSearchBar
import pt.isel.keepmyplanet.ui.map.states.MapEvent
import pt.isel.keepmyplanet.ui.theme.backgroundLight
import pt.isel.keepmyplanet.ui.theme.onSurfaceLight
import pt.isel.keepmyplanet.ui.theme.primaryLight
import pt.isel.keepmyplanet.ui.theme.surfaceLight
import pt.isel.keepmyplanet.utils.latToY
import pt.isel.keepmyplanet.utils.lonToX
import pt.isel.keepmyplanet.utils.xToLon
import pt.isel.keepmyplanet.utils.yToLat

@Composable
fun MapScreen(
    viewModel: MapViewModel,
    onNavigateToZoneDetails: (zoneId: Id) -> Unit,
    onNavigateToReportZone: (latitude: Double, longitude: Double) -> Unit,
    onNavigateBack: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val mapState = viewModel.mapState

    val (currentCalloutId, setCurrentCalloutId) = remember { mutableStateOf<String?>(null) }

    val locationProvider =
        rememberLocationProvider(
            onLocationUpdated = { lat, lon ->
                viewModel.onLocationUpdateReceived(lat, lon)
            },
            onLocationError = {
                viewModel.onLocationError()
            },
        )

    LaunchedEffect(Unit) {
        viewModel.requestLocationPermissionOrUpdate()
    }

    LaunchedEffect(viewModel.events) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is MapEvent.ShowSnackbar -> {
                    snackbarHostState.currentSnackbarData?.dismiss()
                    snackbarHostState.showSnackbar(event.message)
                }

                is MapEvent.RequestLocation -> {
                    if (locationProvider.isPermissionGranted) {
                        locationProvider.requestLocationUpdate()
                    } else {
                        locationProvider.requestPermission()
                        snackbarHostState.showSnackbar(
                            "Location permission is needed to center the map on you.",
                        )
                    }
                }

                is MapEvent.CenterOnUserLocation -> {}
            }
        }
    }

    LaunchedEffect(uiState.selectedZoneId) {
        currentCalloutId?.let {
            mapState.removeCallout(it)
        }

        val newCalloutId = uiState.selectedZoneId
        if (newCalloutId != null) {
            val zone = uiState.zones.find { it.id.value.toString() == newCalloutId }
            if (zone != null) {
                mapState.addCallout(
                    id = newCalloutId,
                    x = lonToX(zone.location.longitude),
                    y = latToY(zone.location.latitude),
                    autoDismiss = false,
                ) {
                    Card(
                        modifier =
                            Modifier
                                .padding(10.dp)
                                .shadow(4.dp, RoundedCornerShape(8.dp))
                                .widthIn(max = 240.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors =
                            CardDefaults.cardColors(
                                containerColor = surfaceLight,
                                contentColor = onSurfaceLight,
                            ),
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            StatusBadge(
                                text = zone.zoneSeverity.name,
                                backgroundColor = getSeverityColor(zone.zoneSeverity),
                            )
                            Text(
                                text = zone.description.value,
                                style = MaterialTheme.typography.bodyLarge,
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.CenterEnd,
                            ) {
                                TextButton(
                                    onClick = { onNavigateToZoneDetails(zone.id) },
                                    colors =
                                        ButtonDefaults.textButtonColors(
                                            containerColor = surfaceLight,
                                            contentColor = onSurfaceLight,
                                        ),
                                ) {
                                    Text("VIEW DETAILS", color = primaryLight)
                                }
                            }
                        }
                    }
                }
            }
        }
        setCurrentCalloutId(newCalloutId)
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = { AppTopBar(title = "Map", onNavigateBack = onNavigateBack) },
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                if (!uiState.isReportingMode && uiState.error == null) {
                    FloatingActionButton(
                        onClick = { viewModel.requestLocationPermissionOrUpdate() },
                        containerColor = MaterialTheme.colorScheme.secondary,
                    ) {
                        if (uiState.isLocatingUser) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onSecondary,
                            )
                        } else {
                            Icon(
                                Icons.Default.MyLocation,
                                contentDescription = "Center on my location",
                            )
                        }
                    }
                    FloatingActionButton(
                        onClick = { viewModel.enterReportingMode() },
                        containerColor = backgroundLight,
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Report Zone",
                            tint = primaryLight,
                        )
                    }
                }
            }
        },
    ) { paddingValues ->
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .pointerInput(Unit) {
                        awaitPointerEventScope {
                            while (true) {
                                val event = awaitPointerEvent()
                                if (event.type == PointerEventType.Scroll) {
                                    val scrollDelta =
                                        event.changes
                                            .first()
                                            .scrollDelta.y
                                    val centroid = event.changes.first().position
                                    val zoomFactor = 1.1f
                                    val scaleMultiplier = zoomFactor.pow(-scrollDelta)

                                    coroutineScope.launch {
                                        val newScale =
                                            (mapState.scale * scaleMultiplier).coerceIn(
                                                mapState.minScale,
                                                mapState.maxScale,
                                            )
                                        val scaleRatio = newScale / mapState.scale
                                        val scroll = mapState.scroll
                                        val newScrollX =
                                            (scroll.x + centroid.x) * scaleRatio - centroid.x
                                        val newScrollY =
                                            (scroll.y + centroid.y) * scaleRatio - centroid.y
                                        mapState.scale = newScale
                                        mapState.setScroll(newScrollX, newScrollY)
                                    }
                                    event.changes.first().consume()
                                }
                            }
                        }
                    },
            contentAlignment = Alignment.Center,
        ) {
            MapUI(modifier = Modifier.fillMaxSize(), state = mapState)

            MapSearchBar(
                query = uiState.searchQuery,
                onQueryChange = viewModel::onSearchQueryChanged,
                onClear = viewModel::clearSearch,
                searchResults = uiState.searchResults,
                isSearching = uiState.isSearching,
                onPlaceSelected = viewModel::onPlaceSelected,
                modifier =
                    Modifier
                        .align(Alignment.TopCenter)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
            )

            if (uiState.isLoading && uiState.zones.isEmpty()) {
                FullScreenLoading()
            } else if (uiState.error != null && uiState.zones.isEmpty()) {
                ErrorState(message = uiState.error!!) {
                    coroutineScope.launch { viewModel.onMapIdle() }
                }
            } else {
                if (uiState.isLoading) {
                    Surface(
                        modifier =
                            Modifier.align(Alignment.BottomCenter).padding(bottom = 160.dp),
                        shape = RoundedCornerShape(16.dp),
                        shadowElevation = 4.dp,
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Loading zones...", style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }

                if (uiState.isReportingMode) {
                    Icon(
                        imageVector = Icons.Default.GpsFixed,
                        contentDescription = "Reporting Pin",
                        modifier = Modifier.size(40.dp),
                        tint = primaryLight,
                    )

                    Surface(
                        modifier =
                            Modifier
                                .align(Alignment.BottomCenter)
                                .padding(horizontal = 16.dp, vertical = 24.dp)
                                .shadow(8.dp, RoundedCornerShape(16.dp)),
                        shape = RoundedCornerShape(16.dp),
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Text(
                                "Report Polluted Zone",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                "Move the map to position the pin on the polluted area.",
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                TextButton(onClick = { viewModel.exitReportingMode() }) {
                                    Text("CANCEL")
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = {
                                        val lon = xToLon(mapState.centroidX)
                                        val lat = yToLat(mapState.centroidY)
                                        onNavigateToReportZone(lat, lon)
                                        viewModel.exitReportingMode()
                                    },
                                ) { Text("CONFIRM") }
                            }
                        }
                    }
                }
            }
        }
    }
}
