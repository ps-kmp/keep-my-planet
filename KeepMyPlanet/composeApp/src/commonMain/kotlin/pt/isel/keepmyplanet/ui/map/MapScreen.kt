package pt.isel.keepmyplanet.ui.map

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import ovh.plrapps.mapcompose.api.addCallout
import ovh.plrapps.mapcompose.api.centroidX
import ovh.plrapps.mapcompose.api.centroidY
import ovh.plrapps.mapcompose.api.fullSize
import ovh.plrapps.mapcompose.api.getLayoutSize
import ovh.plrapps.mapcompose.api.removeCallout
import ovh.plrapps.mapcompose.api.rotation
import ovh.plrapps.mapcompose.api.scale
import ovh.plrapps.mapcompose.api.scroll
import ovh.plrapps.mapcompose.api.setScroll
import ovh.plrapps.mapcompose.ui.MapUI
import ovh.plrapps.mapcompose.utils.toRad
import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.ui.components.AppTopBar
import pt.isel.keepmyplanet.ui.components.ErrorState
import pt.isel.keepmyplanet.ui.components.FullScreenLoading
import pt.isel.keepmyplanet.ui.components.StatusBadge
import pt.isel.keepmyplanet.ui.components.getSeverityColorPair
import pt.isel.keepmyplanet.ui.components.getSeverityColorPairNonComposable
import pt.isel.keepmyplanet.ui.components.rememberLocationProvider
import pt.isel.keepmyplanet.ui.map.components.GuestPromptBanner
import pt.isel.keepmyplanet.ui.map.components.MapSearchBar
import pt.isel.keepmyplanet.ui.map.states.MapEvent
import pt.isel.keepmyplanet.ui.theme.customColors
import pt.isel.keepmyplanet.utils.haversineDistance
import pt.isel.keepmyplanet.utils.latToY
import pt.isel.keepmyplanet.utils.lonToX
import pt.isel.keepmyplanet.utils.xToLon
import pt.isel.keepmyplanet.utils.yToLat

@OptIn(FlowPreview::class)
@Composable
fun MapScreen(
    viewModel: MapViewModel,
    onNavigateToHome: () -> Unit,
    initialLatitude: Double? = null,
    initialLongitude: Double? = null,
    onNavigateToZoneDetails: (zoneId: Id) -> Unit,
    onNavigateToReportZone: (latitude: Double, longitude: Double, radius: Double) -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToLogin: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val mapState = viewModel.mapState
    var showGuestBanner by remember(uiState.isGuest) { mutableStateOf(uiState.isGuest) }

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

    val primaryColor = MaterialTheme.colorScheme.primary
    val customColors = customColors
    val colorScheme = MaterialTheme.colorScheme

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

    LaunchedEffect(initialLatitude, initialLongitude) {
        if (initialLatitude != null && initialLongitude != null) {
            viewModel.centerOnLocation(initialLatitude, initialLongitude)
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
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            ),
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            val (radiusBg, radiusContent) =
                                MaterialTheme.colorScheme.secondaryContainer to
                                    MaterialTheme.colorScheme.onSecondaryContainer
                            StatusBadge(
                                text = "Radius: ${zone.radius.value}m",
                                backgroundColor = radiusBg,
                                contentColor = radiusContent,
                            )
                            val (severityBg, severityContent) =
                                getSeverityColorPair(
                                    zone.zoneSeverity,
                                )
                            StatusBadge(
                                text = zone.zoneSeverity.name,
                                backgroundColor = severityBg,
                                contentColor = severityContent,
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
                                            containerColor =
                                                MaterialTheme.colorScheme.surfaceVariant,
                                            contentColor =
                                                MaterialTheme.colorScheme.onSurfaceVariant,
                                        ),
                                ) {
                                    Text("VIEW DETAILS", color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }
            }
        }
        setCurrentCalloutId(newCalloutId)
    }

    LaunchedEffect(uiState.zones, customColors, colorScheme) {
        viewModel.updateZonePathsOnMap(uiState.zones) { severity ->
            getSeverityColorPairNonComposable(severity, customColors, colorScheme)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            AppTopBar(
                title = "Map",
                onNavigateBack = onNavigateBack,
                onNavigateToHome = onNavigateToHome,
            )
        },
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
                    if (!uiState.isGuest && !uiState.isReportingMode) {
                        FloatingActionButton(
                            onClick = { viewModel.enterReportingMode() },
                            containerColor = MaterialTheme.colorScheme.surface,
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "Report Zone",
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                }
            }
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentAlignment = Alignment.Center,
        ) {
            MapUI(
                modifier =
                    Modifier.fillMaxSize().pointerInput(Unit) {
                        awaitPointerEventScope {
                            while (true) {
                                val event = awaitPointerEvent()
                                if (event.type == PointerEventType.Scroll) {
                                    event.changes.forEach { it.consume() }
                                    val scrollDelta =
                                        event.changes
                                            .first()
                                            .scrollDelta.y
                                    val centroid = event.changes.first().position

                                    coroutineScope.launch {
                                        val zoomFactor = 1.2f
                                        val scaleRatio =
                                            if (scrollDelta < 0) {
                                                zoomFactor.toDouble()
                                            } else {
                                                1.0 / zoomFactor.toDouble()
                                            }

                                        val formerScale = mapState.scale
                                        mapState.scale *= scaleRatio
                                        val newScale = mapState.scale

                                        val effectiveScaleRatio = newScale / formerScale

                                        if (effectiveScaleRatio != 1.0) {
                                            val layoutSize = mapState.getLayoutSize()

                                            val angleRad = -mapState.rotation.toRad().toDouble()

                                            val cx = layoutSize.width / 2.0
                                            val cy = layoutSize.height / 2.0

                                            val centroidX = centroid.x.toDouble()
                                            val centroidY = centroid.y.toDouble()

                                            val rotatedCentroidX =
                                                cx + (centroidX - cx) * cos(angleRad) -
                                                    (centroidY - cy) * sin(angleRad)
                                            val rotatedCentroidY =
                                                cy + (centroidX - cx) * sin(angleRad) +
                                                    (centroidY - cy) * cos(angleRad)

                                            val newScrollX =
                                                (mapState.scroll.x + rotatedCentroidX) *
                                                    effectiveScaleRatio - rotatedCentroidX
                                            val newScrollY =
                                                (mapState.scroll.y + rotatedCentroidY) *
                                                    effectiveScaleRatio - rotatedCentroidY

                                            mapState.setScroll(newScrollX, newScrollY)
                                        }
                                    }
                                }
                            }
                        }
                    },
                state = mapState,
            )

            if (uiState.isReportingMode) {
                val radiusInMeters = uiState.reportingRadius
                val centerX = mapState.centroidX
                val centerY = mapState.centroidY

                val centerLon = xToLon(centerX)
                val centerLat = yToLat(centerY)
                val pointOnRadiusLon =
                    xToLon((centerX * mapState.fullSize.height + 1) / mapState.fullSize.width)
                val metersPerPixel =
                    haversineDistance(centerLat, centerLon, centerLat, pointOnRadiusLon)
                val radiusInDp =
                    if (metersPerPixel > 0) (radiusInMeters / metersPerPixel).dp else 0.dp

                Box(
                    modifier =
                        Modifier
                            .align(Alignment.Center)
                            .size(radiusInDp * 2)
                            .background(primaryColor.copy(alpha = 0.2f), CircleShape)
                            .border(2.dp, primaryColor, CircleShape)
                            .pointerInput(Unit) {},
                )

                Icon(
                    imageVector = Icons.Default.GpsFixed,
                    contentDescription = "Reporting Pin",
                    modifier = Modifier.align(Alignment.Center).size(40.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }

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
                        modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 160.dp),
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
                    Surface(
                        modifier =
                            Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                                .padding(16.dp),
                        shape = RoundedCornerShape(16.dp),
                        shadowElevation = 8.dp,
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Text(
                                text = "Move map to position pin and adjust radius",
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                IconButton(onClick = { viewModel.exitReportingMode() }) {
                                    Icon(Icons.Default.Close, contentDescription = "Cancel Report")
                                }
                                Slider(
                                    value = uiState.reportingRadius.toFloat(),
                                    onValueChange = {
                                        viewModel.onReportingRadiusChange(it.toDouble())
                                    },
                                    valueRange = 10f..500f,
                                    steps = 48,
                                    modifier = Modifier.weight(1f),
                                )
                                IconButton(
                                    onClick = {
                                        val lon = xToLon(mapState.centroidX)
                                        val lat = yToLat(mapState.centroidY)
                                        onNavigateToReportZone(
                                            lat,
                                            lon,
                                            uiState.reportingRadius,
                                        )
                                        viewModel.exitReportingMode()
                                    },
                                ) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = "Confirm Report",
                                    )
                                }
                                Text(
                                    text = "${uiState.reportingRadius.roundToInt()}m",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }
                    }
                }
            }
            GuestPromptBanner(
                isVisible = showGuestBanner,
                onDismiss = { showGuestBanner = false },
                onLoginClick = onNavigateToLogin,
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }
    }
}
