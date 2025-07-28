package pt.isel.keepmyplanet.ui.map

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import ovh.plrapps.mapcompose.api.addPath
import ovh.plrapps.mapcompose.api.hasMarker
import ovh.plrapps.mapcompose.api.idleStateFlow
import ovh.plrapps.mapcompose.api.moveMarker
import ovh.plrapps.mapcompose.api.onMarkerClick
import ovh.plrapps.mapcompose.api.onTap
import ovh.plrapps.mapcompose.api.removeMarker
import ovh.plrapps.mapcompose.api.removePath
import ovh.plrapps.mapcompose.api.scrollTo
import ovh.plrapps.mapcompose.api.visibleBoundingBox
import ovh.plrapps.mapcompose.ui.layout.Fit
import ovh.plrapps.mapcompose.ui.state.MapState
import ovh.plrapps.mapcompose.ui.state.markers.model.RenderingStrategy
import pt.isel.keepmyplanet.data.cache.MapTileCacheRepository
import pt.isel.keepmyplanet.data.repository.GeocodingApiRepository
import pt.isel.keepmyplanet.data.repository.ZoneApiRepository
import pt.isel.keepmyplanet.domain.common.Place
import pt.isel.keepmyplanet.domain.zone.Location
import pt.isel.keepmyplanet.domain.zone.Zone
import pt.isel.keepmyplanet.domain.zone.ZoneSeverity
import pt.isel.keepmyplanet.session.SessionManager
import pt.isel.keepmyplanet.ui.base.BaseViewModel
import pt.isel.keepmyplanet.ui.components.getSeverityColorPair
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
import pt.isel.keepmyplanet.ui.theme.customColors
import pt.isel.keepmyplanet.utils.addCircle
import pt.isel.keepmyplanet.utils.createOfflineFirstTileStreamProvider
import pt.isel.keepmyplanet.utils.haversineDistance
import pt.isel.keepmyplanet.utils.latToY
import pt.isel.keepmyplanet.utils.lonToX
import pt.isel.keepmyplanet.utils.xToLon
import pt.isel.keepmyplanet.utils.yToLat

@OptIn(FlowPreview::class, ExperimentalClusteringApi::class)
class MapViewModel(
    private val httpClient: HttpClient,
    private val zoneRepository: ZoneApiRepository,
    private val geocodingRepository: GeocodingApiRepository,
    private val mapTileCacheRepository: MapTileCacheRepository,
    sessionManager: SessionManager,
) : BaseViewModel<MapUiState>(
        MapUiState(isGuest = sessionManager.userSession.value == null),
        sessionManager,
    ) {
    private val _userLocation = MutableStateFlow<Location?>(null)
    val userLocation = _userLocation.asStateFlow()

    private val displayedZoneIds = MutableStateFlow<Set<String>>(emptySet())
    private val displayedPathIds = MutableStateFlow<Set<String>>(emptySet())
    private val searchQueryFlow = MutableStateFlow("")

    val mapState: MapState =
        MapState(MAX_ZOOM, MAP_DIMENSION, MAP_DIMENSION) {
            scale(INITIAL_SCALE)
            scroll(lonToX(DEFAULT_LON), latToY(DEFAULT_LAT))
            minimumScaleMode(Fit)
            maxScale(2.0)
            preloadingPadding(128)
        }

    private var isInitialLocationSet = false
    private var hasAttemptedInitialLocation = false

    init {
        initializeMap()
    }

    override fun onViewModelCleared() {
        mapState.shutdown()
    }

    private fun loadAllCachedZones() {
        viewModelScope.launch {
            zoneRepository
                .getAllZonesFromCache()
                .onSuccess { cachedZones ->
                    if (cachedZones.isNotEmpty()) {
                        setState { copy(zones = cachedZones, error = null) }
                    }
                }.onFailure {
                    handleErrorWithMessage("Failed to load cached zones.")
                }
        }
    }

    private fun initializeMap() {
        viewModelScope.launch {
            mapState.addLayer(
                createOfflineFirstTileStreamProvider(httpClient, mapTileCacheRepository),
            )
            setupClusterer()
            setupMapListeners()
            loadAllCachedZones()

            mapState
                .idleStateFlow()
                .debounce(500L)
                .distinctUntilChanged()
                .collectLatest { isIdle ->
                    if (isIdle && !currentState.isReportingMode) {
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
                        mapState.scrollTo(x, y, INITIAL_SCALE)
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

    @OptIn(ExperimentalClusteringApi::class)
    private fun setupClusterer() {
        mapState.addClusterer(ZONE_CLUSTER_ID) { clusterIds ->
            @Composable {
                val clusterSize = clusterIds.size
                val (bgColor, textColor, size) =
                    when {
                        clusterSize > 25 ->
                            Triple(
                                MaterialTheme.colorScheme.errorContainer,
                                MaterialTheme.colorScheme.onErrorContainer,
                                50.dp,
                            )

                        clusterSize > 10 ->
                            Triple(
                                customColors.warningContainer,
                                customColors.onWarningContainer,
                                45.dp,
                            )

                        else ->
                            Triple(
                                MaterialTheme.colorScheme.primaryContainer,
                                MaterialTheme.colorScheme.onPrimaryContainer,
                                40.dp,
                            )
                    }

                Box(
                    modifier = Modifier.size(size).background(bgColor, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = clusterSize.toString(),
                        color = textColor,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }

    private fun setupMapListeners() {
        mapState.onMarkerClick { id, _, _ -> onMarkerTapped(id) }
        mapState.onTap { _, _ -> if (!currentState.isReportingMode) hideCallout() }
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

    @OptIn(ExperimentalClusteringApi::class)
    private fun updateZoneMarkersOnMap(zones: List<Zone>) {
        val newZonesMap = zones.associateBy { it.id.value.toString() }
        val newZoneIds = newZonesMap.keys
        val currentZoneIds = displayedZoneIds.value

        val zoneIdsToRemove = currentZoneIds - newZoneIds
        val zoneIdsToAdd = newZoneIds - currentZoneIds

        zoneIdsToRemove.forEach {
            mapState.removeMarker(it)
        }

        zoneIdsToAdd.forEach { id ->
            val zone = newZonesMap[id] ?: return@forEach
            mapState.addMarker(
                id = zone.id.value.toString(),
                x = lonToX(zone.location.longitude),
                y = latToY(zone.location.latitude),
                renderingStrategy = RenderingStrategy.Clustering(ZONE_CLUSTER_ID),
            ) {
                val (color, _) = getSeverityColorPair(zone.zoneSeverity)
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Zone: ${zone.description.value}",
                    tint = color,
                    modifier = Modifier.size(36.dp),
                )
            }
        }
        displayedZoneIds.value = newZoneIds
    }

    fun updateZonePathsOnMap(
        zones: List<Zone>,
        getColors: (ZoneSeverity) -> Pair<Color, Color>,
    ) {
        val newPathIds = zones.map { "path_${it.id.value}" }.toSet()
        val currentPathIds = displayedPathIds.value
        val pathIdsToRemove = currentPathIds - newPathIds
        val zonesToAddPathFor = zones.filter { "path_${it.id.value}" !in currentPathIds }

        pathIdsToRemove.forEach { mapState.removePath(it) }

        zonesToAddPathFor.forEach { zone ->
            val (strokeColor, _) = getColors(zone.zoneSeverity)
            val fillColor = strokeColor.copy(alpha = 0.2f)

            mapState.addPath(
                id = "path_${zone.id.value}",
                width = 1.dp,
                color = strokeColor.copy(alpha = 0.5f),
                fillColor = fillColor,
                clickable = false,
            ) {
                addCircle(zone.location.latitude, zone.location.longitude, zone.radius.value)
            }
        }
        displayedPathIds.value = newPathIds
    }

    fun onReportingRadiusChange(radius: Double) {
        if (radius > 0) {
            setState { copy(reportingRadius = radius) }
        }
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

    fun enterReportingMode() {
        if (currentState.isGuest) {
            sendEvent(MapEvent.ShowSnackbar("Please log in to report a zone."))
            return
        }
        setState {
            copy(isReportingMode = true, selectedZoneId = null)
        }
    }

    fun exitReportingMode() {
        setState { copy(isReportingMode = false) }
    }

    fun onLocationUpdateReceived(
        latitude: Double,
        longitude: Double,
    ) {
        hasAttemptedInitialLocation = true
        _userLocation.value = Location(latitude, longitude)
        setState { copy(isLocatingUser = false) }
    }

    fun onLocationError() {
        setState { copy(isLocatingUser = false) }
        if (hasAttemptedInitialLocation) {
            handleErrorWithMessage("Unable to retrieve your location.")
        }
        hasAttemptedInitialLocation = true
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

    fun centerOnLocation(
        latitude: Double,
        longitude: Double,
    ) {
        viewModelScope.launch {
            mapState.scrollTo(lonToX(longitude), latToY(latitude), INITIAL_SCALE)
        }
    }
}
