package pt.isel.keepmyplanet.ui.map

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarDuration
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import pt.isel.keepmyplanet.ui.components.AppTopBar
import pt.isel.keepmyplanet.ui.components.FullScreenLoading
import pt.isel.keepmyplanet.ui.map.model.MapScreenEvent
import kotlin.time.Duration.Companion.seconds

@Composable
actual fun MapScreen(
    viewModel: MapViewModel,
    onNavigateToZoneDetails: (zoneId: UInt) -> Unit,
    onNavigateBack: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val cameraState = rememberCameraState()
    val styleState = rememberStyleState()
    val snackbarHostState = remember { SnackbarHostState() }

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
    ) { paddingValues ->
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
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
                                geometry = Point(Position(zone.longitude, zone.latitude)),
                                properties =
                                    mutableMapOf(
                                        "zoneId" to JsonPrimitive(zone.id.toString()),
                                        "description" to JsonPrimitive(zone.description),
                                        "severity" to JsonPrimitive(zone.severity),
                                    ),
                            )
                        }

                    val mockMarker1 =
                        Feature(
                            geometry = Point(Position(-9.13333, 38.71667)),
                            properties =
                                mutableMapOf(
                                    "zoneId" to JsonPrimitive("mock-1"),
                                    "description" to JsonPrimitive("This is a mock marker!"),
                                    "severity" to JsonPrimitive("LOW"),
                                ),
                        )

                    val mockMarker2 =
                        Feature(
                            geometry = Point(Position(-9.13, 38.72)),
                            properties =
                                mutableMapOf(
                                    "zoneId" to JsonPrimitive(2),
                                    "description" to JsonPrimitive("This is a mock marker!"),
                                    "severity" to JsonPrimitive("MEDIUM"),
                                ),
                        )

                    val mockMarker3 =
                        Feature(
                            geometry = Point(Position(-9.14, 38.71)),
                            properties =
                                mutableMapOf(
                                    "zoneId" to JsonPrimitive(3),
                                    "description" to JsonPrimitive("This is a mock marker!"),
                                    "severity" to JsonPrimitive("HIGH"),
                                ),
                        )

                    val geoJsonSource =
                        rememberGeoJsonSource(
                            id = "zones-source",
                            data = FeatureCollection(apiFeatures + mockMarker1 + mockMarker2 + mockMarker3),
                        )

                    SymbolLayer(
                        id = "zones-layer",
                        source = geoJsonSource,
                        onClick = { features ->
                            features.firstOrNull()?.let { feature ->
                                val zoneId =
                                    (feature.properties["zoneId"] as? JsonPrimitive)?.content?.toUIntOrNull()
                                if (zoneId != null) {
                                    viewModel.onZoneSelected(zoneId)
                                } else {
                                    (feature.properties["description"] as? JsonPrimitive)?.content?.let {
                                        viewModel.showSnackbar(it)
                                    }
                                }
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
            }
            uiState.error?.let { Text(text = it, modifier = Modifier.padding(16.dp)) }
        }
    }
}
