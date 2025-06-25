package pt.isel.keepmyplanet.ui.map

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlin.math.pow
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.ExperimentalResourceApi
import ovh.plrapps.mapcompose.api.ExperimentalClusteringApi
import ovh.plrapps.mapcompose.api.addCallout
import ovh.plrapps.mapcompose.api.addClusterer
import ovh.plrapps.mapcompose.api.addLayer
import ovh.plrapps.mapcompose.api.addMarker
import ovh.plrapps.mapcompose.api.centroidX
import ovh.plrapps.mapcompose.api.centroidY
import ovh.plrapps.mapcompose.api.enableRotation
import ovh.plrapps.mapcompose.api.getLayoutSize
import ovh.plrapps.mapcompose.api.idleStateFlow
import ovh.plrapps.mapcompose.api.maxScale
import ovh.plrapps.mapcompose.api.minScale
import ovh.plrapps.mapcompose.api.onMarkerClick
import ovh.plrapps.mapcompose.api.onTap
import ovh.plrapps.mapcompose.api.removeCallout
import ovh.plrapps.mapcompose.api.removeMarker
import ovh.plrapps.mapcompose.api.scale
import ovh.plrapps.mapcompose.api.scroll
import ovh.plrapps.mapcompose.api.setScroll
import ovh.plrapps.mapcompose.api.visibleBoundingBox
import ovh.plrapps.mapcompose.ui.MapUI
import ovh.plrapps.mapcompose.ui.layout.Forced
import ovh.plrapps.mapcompose.ui.state.MapState
import ovh.plrapps.mapcompose.ui.state.markers.model.RenderingStrategy
import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.ui.common.FullScreenLoading
import pt.isel.keepmyplanet.ui.components.AppTopBar
import pt.isel.keepmyplanet.ui.components.ErrorState
import pt.isel.keepmyplanet.ui.components.getSeverityColor
import pt.isel.keepmyplanet.ui.map.states.MapEvent
import pt.isel.keepmyplanet.utils.getTileStreamProvider
import pt.isel.keepmyplanet.utils.latToY
import pt.isel.keepmyplanet.utils.lonToX
import pt.isel.keepmyplanet.utils.xToLon
import pt.isel.keepmyplanet.utils.yToLat

private const val TILE_SIZE = 256
private const val MAX_ZOOM = 18
private val MAP_DIMENSION = TILE_SIZE * 2.0.pow(MAX_ZOOM - 1).toInt()

private const val LISBON_LAT = 38.7223
private const val LISBON_LON = -9.1393

@OptIn(ExperimentalResourceApi::class, ExperimentalClusteringApi::class, FlowPreview::class)
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

    val mapState =
        remember {
            MapState(MAX_ZOOM, MAP_DIMENSION, MAP_DIMENSION) {
                scale(12.5)
                scroll(lonToX(LISBON_LON), latToY(LISBON_LAT))
                minimumScaleMode(Forced(0.2))
            }
        }

    val isMapReady by produceState(initialValue = false, mapState) {
        mapState.getLayoutSize()
        value = true
    }

    LaunchedEffect(isMapReady, viewModel) {
        if (isMapReady) {
            mapState
                .idleStateFlow()
                .debounce(500L)
                .collectLatest { isIdle ->
                    if (isIdle) {
                        coroutineScope.launch {
                            viewModel.onMapIdle(mapState.visibleBoundingBox())
                        }
                    }
                }
        }
    }

    LaunchedEffect(viewModel.events) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is MapEvent.ShowSnackbar -> {
                    snackbarHostState.currentSnackbarData?.dismiss()
                    snackbarHostState.showSnackbar(event.message)
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        val tileStreamProvider = getTileStreamProvider()
        mapState.addLayer(tileStreamProvider)
        mapState.enableRotation()

        mapState.addClusterer("zone-clusterer") { clusterIds ->
            {
                Box(
                    modifier =
                        Modifier.size(40.dp).background(MaterialTheme.colors.primary, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = clusterIds.size.toString(),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }

    val displayedZoneIds = remember { mutableStateOf(emptySet<String>()) }

    LaunchedEffect(uiState.zones) {
        val newZonesMap = uiState.zones.associateBy { it.id.value.toString() }
        val newZoneIds = newZonesMap.keys
        val currentZoneIds = displayedZoneIds.value

        val zoneIdsToRemove = currentZoneIds - newZoneIds
        val zoneIdsToAdd = newZoneIds - currentZoneIds

        zoneIdsToRemove.forEach {
            mapState.removeMarker(it)
        }

        zoneIdsToAdd.forEach { id ->
            val zone = newZonesMap[id] ?: return@forEach
            val x = lonToX(zone.location.longitude)
            val y = latToY(zone.location.latitude)

            mapState.addMarker(
                id = zone.id.value.toString(),
                x = x,
                y = y,
                renderingStrategy = RenderingStrategy.Clustering("zone-clusterer"),
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Zone: ${zone.description.value}",
                    tint = getSeverityColor(zone.zoneSeverity),
                    modifier = Modifier.size(36.dp),
                )
            }
        }
        displayedZoneIds.value = newZoneIds
    }

    val (currentCalloutId, setCurrentCalloutId) = remember { mutableStateOf<String?>(null) }

    val dismissCallout = {
        currentCalloutId?.let {
            mapState.removeCallout(it)
            setCurrentCalloutId(null)
        }
    }

    LaunchedEffect(uiState.isReportingMode) {
        if (uiState.isReportingMode) {
            dismissCallout()
        }
    }

    mapState.onMarkerClick { id, _, _ ->
        if (!uiState.isReportingMode) {
            if (currentCalloutId == id) return@onMarkerClick

            dismissCallout()

            val zoneId = Id(id.toUInt())
            val zone = uiState.zones.find { it.id == zoneId } ?: return@onMarkerClick

            setCurrentCalloutId(id)
            mapState.addCallout(
                id = id,
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
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            text = "Zone #${zone.id.value}",
                            style = MaterialTheme.typography.subtitle2,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = zone.description.value,
                            style = MaterialTheme.typography.body2,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.CenterEnd,
                        ) {
                            TextButton(onClick = { onNavigateToZoneDetails(zone.id) }) {
                                Text("VIEW DETAILS")
                            }
                        }
                    }
                }
            }
        }
    }

    mapState.onTap { _, _ ->
        dismissCallout()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            if (!uiState.isReportingMode && uiState.error == null) {
                AppTopBar(title = "Map", onNavigateBack = onNavigateBack)
            }
        },
        floatingActionButton = {
            if (!uiState.isReportingMode && uiState.error == null) {
                FloatingActionButton(onClick = { viewModel.enterReportingMode() }) {
                    Icon(Icons.Default.Add, contentDescription = "Report Zone")
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

            if (uiState.isLoading && uiState.zones.isEmpty()) {
                FullScreenLoading()
            } else if (uiState.error != null && uiState.zones.isEmpty()) {
                ErrorState(message = uiState.error!!) {
                    coroutineScope.launch { viewModel.onMapIdle(mapState.visibleBoundingBox()) }
                }
            } else {
                if (uiState.isLoading) {
                    Surface(
                        modifier = Modifier.align(Alignment.TopCenter).padding(top = 16.dp),
                        shape = RoundedCornerShape(16.dp),
                        elevation = 4.dp,
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
                            Text("Loading zones...", style = MaterialTheme.typography.body2)
                        }
                    }
                }

                if (uiState.isReportingMode) {
                    Icon(
                        imageVector = Icons.Default.GpsFixed,
                        contentDescription = "Reporting Pin",
                        modifier = Modifier.size(40.dp),
                        tint = MaterialTheme.colors.primary,
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
                                text = "Report Polluted Zone",
                                style = MaterialTheme.typography.h6,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                text = "Move the map to position the pin on the polluted area.",
                                style = MaterialTheme.typography.body2,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f),
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
