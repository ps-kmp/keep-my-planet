package pt.isel.keepmyplanet.ui.map

import kotlinx.coroutines.launch
import ovh.plrapps.mapcompose.api.visibleBoundingBox
import pt.isel.keepmyplanet.data.api.ZoneApi
import pt.isel.keepmyplanet.mapper.zone.toZone
import pt.isel.keepmyplanet.ui.base.BaseViewModel
import pt.isel.keepmyplanet.ui.map.model.MapEvent
import pt.isel.keepmyplanet.ui.map.model.MapUiState
import pt.isel.keepmyplanet.ui.map.util.haversineDistance
import pt.isel.keepmyplanet.ui.map.util.xToLon
import pt.isel.keepmyplanet.ui.map.util.yToLat

class MapViewModel(
    private val zoneApi: ZoneApi,
) : BaseViewModel<MapUiState>(MapUiState()) {
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

    fun onRetry(mapState: ovh.plrapps.mapcompose.ui.state.MapState) {
        viewModelScope.launch {
            onMapIdle(mapState.visibleBoundingBox())
        }
    }
}
