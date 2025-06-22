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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlin.math.pow
import kotlinx.coroutines.flow.collectLatest
import org.jetbrains.compose.resources.ExperimentalResourceApi
import ovh.plrapps.mapcompose.api.ExperimentalClusteringApi
import ovh.plrapps.mapcompose.api.addCallout
import ovh.plrapps.mapcompose.api.addClusterer
import ovh.plrapps.mapcompose.api.addLayer
import ovh.plrapps.mapcompose.api.addMarker
import ovh.plrapps.mapcompose.api.centroidX
import ovh.plrapps.mapcompose.api.centroidY
import ovh.plrapps.mapcompose.api.onMarkerClick
import ovh.plrapps.mapcompose.api.onTap
import ovh.plrapps.mapcompose.api.removeCallout
import ovh.plrapps.mapcompose.api.removeMarker
import ovh.plrapps.mapcompose.ui.MapUI
import ovh.plrapps.mapcompose.ui.state.MapState
import ovh.plrapps.mapcompose.ui.state.markers.model.RenderingStrategy
import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.ui.components.AppTopBar
import pt.isel.keepmyplanet.ui.components.ErrorState
import pt.isel.keepmyplanet.ui.components.FullScreenLoading
import pt.isel.keepmyplanet.ui.map.model.MapEvent
import pt.isel.keepmyplanet.ui.map.model.getTileStreamProvider
import pt.isel.keepmyplanet.ui.map.model.latToY
import pt.isel.keepmyplanet.ui.map.model.lonToX
import pt.isel.keepmyplanet.ui.map.model.xToLon
import pt.isel.keepmyplanet.ui.map.model.yToLat
import pt.isel.keepmyplanet.ui.zone.components.getSeverityColor

private const val TILE_SIZE = 256
private const val MAX_ZOOM = 18
private val MAP_DIMENSION = TILE_SIZE * 2.0.pow(MAX_ZOOM - 1).toInt()

private const val LISBON_LAT = 38.7223
private const val LISBON_LON = -9.1393

@OptIn(ExperimentalResourceApi::class, ExperimentalClusteringApi::class)
@Composable
fun MapScreen(
    viewModel: MapViewModel,
    onNavigateToZoneDetails: (zoneId: Id) -> Unit,
    onNavigateToReportZone: (latitude: Double, longitude: Double) -> Unit,
    onNavigateBack: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    val mapState =
        remember {
            MapState(MAX_ZOOM, MAP_DIMENSION, MAP_DIMENSION) {
                scale(1.0 / TILE_SIZE)
                scroll(lonToX(LISBON_LON), latToY(LISBON_LAT))
            }
        }

    LaunchedEffect(viewModel.events) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is MapEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    LaunchedEffect(Unit) {
        val tileStreamProvider = getTileStreamProvider()
        mapState.addLayer(tileStreamProvider)

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
                    modifier = Modifier.padding(10.dp).shadow(4.dp, RoundedCornerShape(8.dp)),
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp).widthIn(max = 220.dp),
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
                            TextButton(
                                onClick = {
                                    dismissCallout()
                                    onNavigateToZoneDetails(zone.id)
                                },
                            ) {
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
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentAlignment = Alignment.Center,
        ) {
            if (uiState.error != null) {
                ErrorState(message = uiState.error!!, onRetry = viewModel::loadZones)
            } else {
                MapUI(modifier = Modifier.fillMaxSize(), state = mapState)

                if (uiState.isLoading) {
                    FullScreenLoading()
                }

                if (uiState.isReportingMode) {
                    Icon(
                        imageVector = Icons.Default.GpsFixed,
                        contentDescription = "Reporting Pin",
                        modifier = Modifier.size(36.dp).align(Alignment.Center),
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
                                TextButton(
                                    onClick = { viewModel.exitReportingMode() },
                                ) { Text("CANCEL") }
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
