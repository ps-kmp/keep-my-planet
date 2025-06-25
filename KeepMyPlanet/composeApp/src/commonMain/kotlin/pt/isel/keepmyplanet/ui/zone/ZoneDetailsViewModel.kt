package pt.isel.keepmyplanet.ui.zone

import pt.isel.keepmyplanet.data.api.ZoneApi
import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.mapper.zone.toZone
import pt.isel.keepmyplanet.ui.viewmodel.BaseViewModel
import pt.isel.keepmyplanet.ui.zone.states.ZoneDetailsEvent
import pt.isel.keepmyplanet.ui.zone.states.ZoneDetailsUiState

class ZoneDetailsViewModel(
    private val zoneApi: ZoneApi,
) : BaseViewModel<ZoneDetailsUiState>(ZoneDetailsUiState()) {
    override fun handleErrorWithMessage(message: String) {
        sendEvent(ZoneDetailsEvent.ShowSnackbar(message))
    }

    fun loadZoneDetails(zoneId: Id) {
        launchWithResult(
            onStart = { copy(isLoading = true, zone = null, error = null) },
            onFinally = { copy(isLoading = false) },
            block = { zoneApi.getZoneDetails(zoneId.value) },
            onSuccess = { response -> setState { copy(zone = response.toZone()) } },
            onError = {
                setState { copy(error = getErrorMessage("Failed to load zone details", it)) }
            },
        )
    }
}
