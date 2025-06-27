package pt.isel.keepmyplanet.ui.zone.details

import kotlinx.coroutines.launch
import pt.isel.keepmyplanet.data.api.PhotoApi
import pt.isel.keepmyplanet.data.api.ZoneApi
import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.domain.user.UserInfo
import pt.isel.keepmyplanet.mapper.zone.toZone
import pt.isel.keepmyplanet.session.SessionManager
import pt.isel.keepmyplanet.ui.viewmodel.BaseViewModel
import pt.isel.keepmyplanet.ui.zone.details.states.ZoneDetailsEvent
import pt.isel.keepmyplanet.ui.zone.details.states.ZoneDetailsUiState

class ZoneDetailsViewModel(
    private val zoneApi: ZoneApi,
    private val photoApi: PhotoApi,
    private val sessionManager: SessionManager,
) : BaseViewModel<ZoneDetailsUiState>(ZoneDetailsUiState()) {

    private val currentUser: UserInfo?
        get() = sessionManager.userSession.value?.userInfo

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
                setState {
                    copy(
                        zone = zone,
                        canUserManageZone = zone.reporterId == currentUser?.id,
                    )
                }
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
    fun deleteZone() {
        val zoneId = currentState.zone?.id ?: return

        launchWithResult(
            onStart = { copy(actionState = ZoneDetailsUiState.ActionState.DELETING) },
            onFinally = { copy(actionState = ZoneDetailsUiState.ActionState.IDLE) },
            block = { zoneApi.deleteZone(zoneId.value) },
            onSuccess = {
                sendEvent(ZoneDetailsEvent.ShowSnackbar("Zone deleted successfully"))
                sendEvent(ZoneDetailsEvent.ZoneDeleted)
            },
            onError = { handleErrorWithMessage(getErrorMessage("Failed to delete zone", it)) },
        )
    }
}
