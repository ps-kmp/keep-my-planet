package pt.isel.keepmyplanet.ui.zone.details

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import pt.isel.keepmyplanet.data.repository.DefaultPhotoRepository
import pt.isel.keepmyplanet.data.repository.DefaultZoneRepository
import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.domain.user.UserInfo
import pt.isel.keepmyplanet.session.SessionManager
import pt.isel.keepmyplanet.ui.base.BaseViewModel
import pt.isel.keepmyplanet.ui.zone.details.states.ZoneDetailsEvent
import pt.isel.keepmyplanet.ui.zone.details.states.ZoneDetailsUiState

class ZoneDetailsViewModel(
    private val zoneRepository: DefaultZoneRepository,
    private val photoRepository: DefaultPhotoRepository,
    private val sessionManager: SessionManager,
) : BaseViewModel<ZoneDetailsUiState>(ZoneDetailsUiState()) {
    private val currentUser: UserInfo?
        get() = sessionManager.userSession.value?.userInfo

    override fun handleErrorWithMessage(message: String) {
        sendEvent(ZoneDetailsEvent.ShowSnackbar(message))
    }

    fun loadZoneDetails(zoneId: Id) {
        launchWithResult(
            onStart = { copy(isLoading = true, error = null) },
            onFinally = { copy(isLoading = false) },
            block = { zoneRepository.getZoneDetails(zoneId) },
            onSuccess = { zone ->
                setState {
                    copy(
                        zone = zone,
                        canUserManageZone = zone.reporterId == currentUser?.id,
                    )
                }
                fetchAndCachePhotos(zone.photosIds)
            },
            onError = {
                if (currentState.zone == null) {
                    setState { copy(error = getErrorMessage("Failed to load zone details", it)) }
                } else {
                    handleErrorWithMessage(getErrorMessage("Failed to refresh zone details", it))
                }
            },
        )
    }

    private suspend fun fetchAndCachePhotos(photoIds: Set<Id>) {
        val fetchedPhotoModels = mutableMapOf<Id, Any>()
        coroutineScope {
            photoIds
                .map { id ->
                    async {
                        photoRepository.getPhotoModel(id).onSuccess { model ->
                            fetchedPhotoModels[id] = model
                        }
                    }
                }.awaitAll()
        }
        setState { copy(photoModels = fetchedPhotoModels) }
    }

    fun deleteZone() {
        val zoneId = currentState.zone?.id ?: return

        launchWithResult(
            onStart = { copy(actionState = ZoneDetailsUiState.ActionState.DELETING) },
            onFinally = { copy(actionState = ZoneDetailsUiState.ActionState.IDLE) },
            block = { zoneRepository.deleteZone(zoneId) },
            onSuccess = {
                sendEvent(ZoneDetailsEvent.ShowSnackbar("Zone deleted successfully"))
                sendEvent(ZoneDetailsEvent.ZoneDeleted)
            },
            onError = { handleErrorWithMessage(getErrorMessage("Failed to delete zone", it)) },
        )
    }
}
