package pt.isel.keepmyplanet.ui.zone

import kotlinx.coroutines.launch
import pt.isel.keepmyplanet.data.api.PhotoApi
import pt.isel.keepmyplanet.data.api.ZoneApi
import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.mapper.zone.toZone
import pt.isel.keepmyplanet.ui.viewmodel.BaseViewModel
import pt.isel.keepmyplanet.ui.zone.states.ZoneDetailsEvent
import pt.isel.keepmyplanet.ui.zone.states.ZoneDetailsUiState

class ZoneDetailsViewModel(
    private val zoneApi: ZoneApi,
    private val photoApi: PhotoApi,
) : BaseViewModel<ZoneDetailsUiState>(ZoneDetailsUiState()) {
    override fun handleErrorWithMessage(message: String) {
        sendEvent(ZoneDetailsEvent.ShowSnackbar(message))
    }

    fun loadZoneDetails(zoneId: Id) {
        launchWithResult(
            onStart = { copy(isLoading = true, zone = null, error = null) },
            onFinally = { copy(isLoading = false) },
            block = { zoneApi.getZoneDetails(zoneId.value) },
            onSuccess = { response ->
                val zone = response.toZone()
                setState { copy(zone = zone) }
                if (zone.photosIds.isNotEmpty()) {
                    fetchPhotoUrls(zone.photosIds)
                }
            },
            onError = {
                setState { copy(error = getErrorMessage("Failed to load zone details", it)) }
            },
        )
    }

    private fun fetchPhotoUrls(photoIds: Set<Id>) {
        viewModelScope.launch {
            val urls =
                photoIds.mapNotNull { id ->
                    photoApi.getPhotoById(id).getOrNull()?.url
                }
            setState { copy(photoUrls = urls) }
        }
    }
}
