package pt.isel.keepmyplanet.ui.map

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import pt.isel.keepmyplanet.data.api.ZoneApi
import pt.isel.keepmyplanet.domain.zone.Location
import pt.isel.keepmyplanet.mapper.zone.toZone
import pt.isel.keepmyplanet.ui.map.states.MapEvent
import pt.isel.keepmyplanet.ui.map.states.MapUiState
import pt.isel.keepmyplanet.ui.viewmodel.BaseViewModel
import pt.isel.keepmyplanet.utils.haversineDistance
import pt.isel.keepmyplanet.utils.xToLon
import pt.isel.keepmyplanet.utils.yToLat

class MapViewModel(
    private val zoneApi: ZoneApi,
) : BaseViewModel<MapUiState>(MapUiState()) {
    private val _userLocation = MutableStateFlow<Location?>(null)
    val userLocation = _userLocation.asStateFlow()

    override fun handleErrorWithMessage(message: String) {
        sendEvent(MapEvent.ShowSnackbar(message))
    }

    fun onMapIdle(bbox: ovh.plrapps.mapcompose.api.BoundingBox) {
        val centerLat = yToLat((bbox.yTop + bbox.yBottom) / 2)
        val centerLon = xToLon((bbox.xLeft + bbox.xRight) / 2)

        val cornerLat = yToLat(bbox.yTop)
        val cornerLon = xToLon(bbox.xLeft)

        val radiusInMeters = haversineDistance(centerLat, centerLon, cornerLat, cornerLon)

        if (radiusInMeters > 50_000) return

        launchWithResult(
            onStart = { copy(isLoading = true) },
            onFinally = { copy(isLoading = false) },
            block = { zoneApi.findZonesByLocation(centerLat, centerLon, radiusInMeters) },
            onSuccess = { response ->
                setState {
                    val newZones = response.map { it.toZone() }
                    val updatedZones = (this.zones + newZones).distinctBy { it.id }
                    copy(zones = updatedZones, error = null)
                }
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

    fun enterReportingMode() =
        setState {
            copy(isReportingMode = true)
        }

    fun exitReportingMode() =
        setState {
            copy(isReportingMode = false)
        }

    fun onLocationUpdateReceived(
        latitude: Double,
        longitude: Double,
    ) {
        _userLocation.value = Location(latitude, longitude)
        sendEvent(MapEvent.CenterOnUserLocation)
        setState { copy(isLocatingUser = false) }
    }

    fun requestLocationPermissionOrUpdate() {
        setState { copy(isLocatingUser = true) }
        sendEvent(MapEvent.RequestLocation)
    }
}
