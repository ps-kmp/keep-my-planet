package pt.isel.keepmyplanet.ui.map

import pt.isel.keepmyplanet.data.api.ZoneApi
import pt.isel.keepmyplanet.mapper.zone.toZone
import pt.isel.keepmyplanet.ui.base.BaseViewModel
import pt.isel.keepmyplanet.ui.map.model.MapEvent
import pt.isel.keepmyplanet.ui.map.model.MapUiState

class MapViewModel(
    private val zoneApi: ZoneApi,
) : BaseViewModel<MapUiState>(MapUiState()) {
    init {
        loadZones()
    }

    override fun handleErrorWithMessage(message: String) {
        sendEvent(MapEvent.ShowSnackbar(message))
    }

    fun enterReportingMode() =
        setState {
            copy(isReportingMode = true)
        }

    fun exitReportingMode() =
        setState {
            copy(isReportingMode = false)
        }

    fun loadZones() {
        launchWithResult(
            onStart = { copy(isLoading = true, error = null) },
            onFinally = { copy(isLoading = false) },
            block = { zoneApi.findAllZones() },
            onSuccess = { response -> setState { copy(zones = response.map { it.toZone() }) } },
            onError = { setState { copy(error = getErrorMessage("Failed to load zones", it)) } },
        )
    }
}
