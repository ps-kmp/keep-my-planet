package pt.isel.keepmyplanet.ui.map

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.ktor.client.HttpClient
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import ovh.plrapps.mapcompose.api.ExperimentalClusteringApi
import ovh.plrapps.mapcompose.api.addClusterer
import ovh.plrapps.mapcompose.api.addLayer
import ovh.plrapps.mapcompose.api.addMarker
import ovh.plrapps.mapcompose.api.hasMarker
import ovh.plrapps.mapcompose.api.idleStateFlow
import ovh.plrapps.mapcompose.api.moveMarker
import ovh.plrapps.mapcompose.api.onMarkerClick
import ovh.plrapps.mapcompose.api.onTap
import ovh.plrapps.mapcompose.api.removeMarker
import ovh.plrapps.mapcompose.api.scrollTo
import ovh.plrapps.mapcompose.api.visibleBoundingBox
import ovh.plrapps.mapcompose.ui.layout.Forced
import ovh.plrapps.mapcompose.ui.state.MapState
import ovh.plrapps.mapcompose.ui.state.markers.model.RenderingStrategy
import pt.isel.keepmyplanet.data.cache.MapTileCacheRepository
import pt.isel.keepmyplanet.data.repository.DefaultGeocodingRepository
import pt.isel.keepmyplanet.data.repository.DefaultZoneRepository
import pt.isel.keepmyplanet.domain.common.Place
import pt.isel.keepmyplanet.domain.zone.Location
import pt.isel.keepmyplanet.domain.zone.Zone
import pt.isel.keepmyplanet.ui.base.BaseViewModel
import pt.isel.keepmyplanet.ui.components.getSeverityColor
import pt.isel.keepmyplanet.ui.components.shouldShowUserLocationMarker
import pt.isel.keepmyplanet.ui.map.MapConfiguration.DEFAULT_LAT
import pt.isel.keepmyplanet.ui.map.MapConfiguration.DEFAULT_LON
import pt.isel.keepmyplanet.ui.map.MapConfiguration.INITIAL_SCALE
import pt.isel.keepmyplanet.ui.map.MapConfiguration.MAP_DIMENSION
import pt.isel.keepmyplanet.ui.map.MapConfiguration.MAX_ZOOM
import pt.isel.keepmyplanet.ui.map.MapConfiguration.USER_LOCATION_MARKER_ID
import pt.isel.keepmyplanet.ui.map.MapConfiguration.ZONE_CLUSTER_ID
import pt.isel.keepmyplanet.ui.map.components.UserLocationMarker
import pt.isel.keepmyplanet.ui.map.states.MapEvent
import pt.isel.keepmyplanet.ui.map.states.MapUiState
import pt.isel.keepmyplanet.ui.theme.primaryLight
import pt.isel.keepmyplanet.utils.createOfflineFirstTileStreamProvider
import pt.isel.keepmyplanet.utils.haversineDistance
import pt.isel.keepmyplanet.utils.latToY
import pt.isel.keepmyplanet.utils.lonToX
import pt.isel.keepmyplanet.utils.xToLon
import pt.isel.keepmyplanet.utils.yToLat

@OptIn(FlowPreview::class, ExperimentalClusteringApi::class)
class MapViewModel(
    private val httpClient: HttpClient,
    private val zoneRepository: DefaultZoneRepository,
    private val geocodingRepository: DefaultGeocodingRepository,
    private val mapTileCacheRepository: MapTileCacheRepository,
) : BaseViewModel<MapUiState>(MapUiState()) {
    private val _userLocation = MutableStateFlow<Location?>(null)
    val userLocation = _userLocation.asStateFlow()

    private val displayedZoneIds = MutableStateFlow<Set<String>>(emptySet())
    private val searchQueryFlow = MutableStateFlow("")

    val mapState: MapState =
        MapState(MAX_ZOOM, MAP_DIMENSION, MAP_DIMENSION) {
            scale(INITIAL_SCALE)
            scroll(lonToX(DEFAULT_LON), latToY(DEFAULT_LAT))
            minimumScaleMode(Forced(0.2))
            preloadingPadding(128)
        }

    private var isInitialLocationSet = false

    init {
        initializeMap()
    }

    override fun onCleared() {
        super.onCleared()
        mapState.shutdown()
    }

    private fun initializeMap() {
        viewModelScope.launch {
            mapState.addLayer(
                createOfflineFirstTileStreamProvider(httpClient, mapTileCacheRepository),
            )
            setupClusterer()
            setupMapListeners()

            mapState
                .idleStateFlow()
                .debounce(500L)
                .distinctUntilChanged()
                .collectLatest { isIdle ->
                    if (isIdle) {
                        onMapIdle()
                    }
                }
        }

        viewModelScope.launch {
            searchQueryFlow
                .debounce(500L)
                .distinctUntilChanged()
                .collectLatest { query ->
                    if (query.length < 3) {
                        setState { copy(searchResults = emptyList(), isSearching = false) }
                    } else {
                        searchAddress(query)
                    }
                }
        }

        viewModelScope.launch {
            userLocation.collectLatest { location ->
                if (location != null) {
                    val x = lonToX(location.longitude)
                    val y = latToY(location.latitude)
                    if (shouldShowUserLocationMarker) {
                        if (mapState.hasMarker(USER_LOCATION_MARKER_ID)) {
                            mapState.moveMarker(USER_LOCATION_MARKER_ID, x, y)
                        } else {
                            mapState.addMarker(USER_LOCATION_MARKER_ID, x, y) {
                                UserLocationMarker()
                            }
                        }
                    }
                    if (!isInitialLocationSet) {
                        mapState.scrollTo(x, y, 16.0)
                        isInitialLocationSet = true
                    }
                }
            }
        }

        viewModelScope.launch {
            uiState.collectLatest { state ->
                updateZoneMarkersOnMap(state.zones)
            }
        }
    }

    private fun setupClusterer() {
        mapState.addClusterer(ZONE_CLUSTER_ID) { clusterIds ->
            {
                Box(
                    modifier =
                        Modifier.size(40.dp).background(primaryLight, CircleShape),
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

    private fun setupMapListeners() {
        mapState.onMarkerClick { id, _, _ -> onMarkerTapped(id) }
        mapState.onTap { _, _ -> hideCallout() }
    }

    suspend fun onMapIdle() {
        val bbox = mapState.visibleBoundingBox()

        val centerLat = yToLat((bbox.yTop + bbox.yBottom) / 2)
        val centerLon = xToLon((bbox.xLeft + bbox.xRight) / 2)
        val cornerLat = yToLat(bbox.yTop)
        val cornerLon = xToLon(bbox.xLeft)
        val radiusInMeters = haversineDistance(centerLat, centerLon, cornerLat, cornerLon)

        if (radiusInMeters > 50_000) return

        launchWithResult(
            onStart = { copy(isLoading = true) },
            onFinally = { copy(isLoading = false) },
            block = { zoneRepository.findZonesByLocation(centerLat, centerLon, radiusInMeters) },
            onSuccess = { newZones ->
                val updatedZones = (currentState.zones + newZones).distinctBy { it.id }
                setState { copy(zones = updatedZones, error = null) }
            },
            onError = {
                val errorMsg = getErrorMessage("Failed to load zones", it)
                if (currentState.zones.isEmpty()) {
                    setState { copy(error = errorMsg) }
                } else {
                    handleErrorWithMessage(errorMsg)
                }
            },
        )
    }

    private fun updateZoneMarkersOnMap(zones: List<Zone>) {
        val newZonesMap = zones.associateBy { it.id.value.toString() }
        val newZoneIds = newZonesMap.keys
        val currentZoneIds = displayedZoneIds.value

        val zoneIdsToRemove = currentZoneIds - newZoneIds
        val zoneIdsToAdd = newZoneIds - currentZoneIds

        zoneIdsToRemove.forEach { mapState.removeMarker(it) }

        zoneIdsToAdd.forEach { id ->
            val zone = newZonesMap[id] ?: return@forEach
            mapState.addMarker(
                id = zone.id.value.toString(),
                x = lonToX(zone.location.longitude),
                y = latToY(zone.location.latitude),
                renderingStrategy = RenderingStrategy.Clustering(ZONE_CLUSTER_ID),
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

    fun onMarkerTapped(id: String) {
        if (id == USER_LOCATION_MARKER_ID) return

        if (currentState.isReportingMode) {
            hideCallout()
            return
        }
        setState { copy(selectedZoneId = id) }
    }

    fun hideCallout() {
        setState { copy(selectedZoneId = null) }
    }

    fun enterReportingMode() =
        setState {
            hideCallout()
            copy(isReportingMode = true)
        }

    fun exitReportingMode() = setState { copy(isReportingMode = false) }

    fun onLocationUpdateReceived(
        latitude: Double,
        longitude: Double,
    ) {
        _userLocation.value = Location(latitude, longitude)
        setState { copy(isLocatingUser = false) }
    }

    fun requestLocationPermissionOrUpdate() {
        setState { copy(isLocatingUser = true) }
        sendEvent(MapEvent.RequestLocation)
    }

    override fun handleErrorWithMessage(message: String) {
        sendEvent(MapEvent.ShowSnackbar(message))
    }

    fun onSearchQueryChanged(query: String) {
        setState { copy(searchQuery = query) }
        searchQueryFlow.value = query
    }

    private fun searchAddress(query: String) {
        launchWithResult(
            onStart = { copy(isSearching = true) },
            onFinally = { copy(isSearching = false) },
            block = { geocodingRepository.search(query) },
            onSuccess = { results -> setState { copy(searchResults = results) } },
            onError = {
                handleErrorWithMessage(getErrorMessage("Failed to search for address", it))
                setState { copy(searchResults = emptyList()) }
            },
        )
    }

    fun onPlaceSelected(place: Place) {
        viewModelScope.launch {
            val x = lonToX(place.longitude)
            val y = latToY(place.latitude)
            mapState.scrollTo(x, y, 16.5)
            clearSearch()
        }
    }

    fun clearSearch() {
        setState { copy(searchQuery = "", searchResults = emptyList(), isSearching = false) }
        searchQueryFlow.value = ""
    }
}
