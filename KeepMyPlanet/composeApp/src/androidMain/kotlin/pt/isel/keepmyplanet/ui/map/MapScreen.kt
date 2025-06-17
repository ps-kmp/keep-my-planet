package pt.isel.keepmyplanet.ui.map

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Button
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarDuration
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddLocation
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.sargunv.maplibrecompose.compose.ClickResult
import dev.sargunv.maplibrecompose.compose.MaplibreMap
import dev.sargunv.maplibrecompose.compose.layer.SymbolLayer
import dev.sargunv.maplibrecompose.compose.rememberCameraState
import dev.sargunv.maplibrecompose.compose.rememberStyleState
import dev.sargunv.maplibrecompose.compose.source.rememberGeoJsonSource
import dev.sargunv.maplibrecompose.core.CameraPosition
import dev.sargunv.maplibrecompose.expressions.dsl.asString
import dev.sargunv.maplibrecompose.expressions.dsl.case
import dev.sargunv.maplibrecompose.expressions.dsl.const
import dev.sargunv.maplibrecompose.expressions.dsl.feature
import dev.sargunv.maplibrecompose.expressions.dsl.image
import dev.sargunv.maplibrecompose.expressions.dsl.switch
import io.github.dellisd.spatialk.geojson.Feature
import io.github.dellisd.spatialk.geojson.FeatureCollection
import io.github.dellisd.spatialk.geojson.Point
import io.github.dellisd.spatialk.geojson.Position
import keepmyplanet.composeapp.generated.resources.Res
import keepmyplanet.composeapp.generated.resources.ic_marker_high
import keepmyplanet.composeapp.generated.resources.ic_marker_low
import keepmyplanet.composeapp.generated.resources.ic_marker_mid
import kotlinx.coroutines.flow.collectLatest
import kotlinx.serialization.json.JsonPrimitive
import org.jetbrains.compose.resources.painterResource
import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.ui.components.AppTopBar
import pt.isel.keepmyplanet.ui.components.FullScreenLoading
import pt.isel.keepmyplanet.ui.map.model.MapScreenEvent
import kotlin.time.Duration.Companion.seconds

@Suppress("ktlint:standard:function-naming")
@Composable
actual fun MapScreen(
    viewModel: MapViewModel,
    onNavigateToZoneDetails: (zoneId: Id) -> Unit,
    onNavigateToReportZone: (latitude: Double, longitude: Double) -> Unit,
    onNavigateBack: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val cameraState = rememberCameraState()
    val styleState = rememberStyleState()
    val snackbarHostState = remember { SnackbarHostState() }
    var isPositioningMode by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        cameraState.animateTo(
            CameraPosition(target = Position(-9.135, 38.715), zoom = 15.0),
            duration = 1.5.seconds,
        )
    }

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is MapScreenEvent.NavigateToZoneDetails -> onNavigateToZoneDetails(event.zoneId)
                is MapScreenEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(event.message, duration = SnackbarDuration.Short)
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = { AppTopBar(title = "Map", onNavigateBack = onNavigateBack) },
        floatingActionButton = {
            if (!isPositioningMode) {
                FloatingActionButton(onClick = { isPositioningMode = true }) {
                    Icon(Icons.Default.AddLocation, contentDescription = "Report a New Zone")
                }
            }
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentAlignment = Alignment.Center,
        ) {
            if (uiState.isLoading) {
                FullScreenLoading()
            } else {
                MaplibreMap(
                    modifier = Modifier.fillMaxSize(),
                    styleUri = "https://basemaps.cartocdn.com/gl/voyager-gl-style/style.json",
                    cameraState = cameraState,
                    styleState = styleState,
                ) {
                    val apiFeatures =
                        uiState.zones.map { zone ->
                            Feature(
                                geometry =
                                    Point(
                                        Position(zone.location.longitude, zone.location.latitude),
                                    ),
                                properties =
                                    mutableMapOf(
                                        "zoneId" to JsonPrimitive(zone.id.value.toString()),
                                        "description" to JsonPrimitive(zone.description.value),
                                        "severity" to JsonPrimitive(zone.zoneSeverity.name),
                                    ),
                            )
                        }

                    val geoJsonSource =
                        rememberGeoJsonSource(
                            id = "zones-source",
                            data = FeatureCollection(apiFeatures),
                        )

                    SymbolLayer(
                        id = "zones-layer",
                        source = geoJsonSource,
                        onClick = { features ->
                            features.firstOrNull()?.let { feature ->
                                val zoneId =
                                    (feature.properties["zoneId"] as? JsonPrimitive)
                                        ?.content
                                        ?.toUIntOrNull()
                                        ?.let { Id(it) }
                                zoneId?.let { viewModel.onZoneSelected(it) }
                            }
                            ClickResult.Consume
                        },
                        iconImage =
                            switch(
                                input = feature.get("severity").asString(),
                                case("LOW", image(painterResource(Res.drawable.ic_marker_low))),
                                case("MEDIUM", image(painterResource(Res.drawable.ic_marker_mid))),
                                case("HIGH", image(painterResource(Res.drawable.ic_marker_high))),
                                fallback = image(painterResource(Res.drawable.ic_marker_high)),
                            ),
                        iconSize = const(0.8f),
                        iconAllowOverlap = const(true),
                    )
                }

                if (isPositioningMode) {
                    Icon(
                        imageVector = Icons.Default.GpsFixed,
                        contentDescription = "Crosshair",
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colors.primary,
                    )

                    Column(
                        modifier =
                            Modifier
                                .align(Alignment.BottomCenter)
                                .padding(16.dp)
                                .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Button(
                            onClick = {
                                val centerPosition = cameraState.position.target
                                onNavigateToReportZone(
                                    centerPosition.latitude,
                                    centerPosition.longitude,
                                )
                                isPositioningMode = false
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text("Confirm Location")
                        }
                        OutlinedButton(
                            onClick = { isPositioningMode = false },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text("Cancel")
                        }
                    }
                }
            }
        }
    }
}
