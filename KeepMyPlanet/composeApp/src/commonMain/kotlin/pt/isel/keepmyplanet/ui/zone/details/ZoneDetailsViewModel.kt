package pt.isel.keepmyplanet.ui.zone.details

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
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
            block = { zoneRepository.getZoneDetailsBundle(zoneId) },
            onSuccess = { bundle ->
                setState {
                    copy(
                        zone = bundle.zone,
                        reporter = bundle.reporter,
                        canUserManageZone = bundle.zone.reporterId == currentUser?.id,
                    )
                }
                fetchAndCachePhotos(bundle.zone.beforePhotosIds + bundle.zone.afterPhotosIds)
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
                .map { photoId ->
                    async {
                        photoRepository.getPhotoModel(photoId).onSuccess { model ->
                            fetchedPhotoModels[photoId] = model
                        }
                    }
                }.awaitAll()
        }
        setState { copy(photoModels = fetchedPhotoModels) }
    }

    fun addAfterPhoto(
        imageData: ByteArray,
        filename: String,
    ) {
        val zone = currentState.zone ?: return
        viewModelScope.launch {
            setState { copy(actionState = ZoneDetailsUiState.ActionState.ADDINGPHOTO) }
            photoRepository
                .createPhoto(imageData, filename)
                .onSuccess { photoResponse ->
                    zoneRepository
                        .addPhotoToZone(zone.id, Id(photoResponse.id), "AFTER")
                        .onSuccess { updatedZone ->
                            sendEvent(ZoneDetailsEvent.ShowSnackbar("Photo added successfully!"))
                            loadZoneDetails(updatedZone.id)
                        }.onFailure {
                            handleErrorWithMessage(
                                getErrorMessage("Failed to add photo to zone", it),
                            )
                        }
                }.onFailure {
                    handleErrorWithMessage(getErrorMessage("Failed to upload photo", it))
                }

            setState { copy(actionState = ZoneDetailsUiState.ActionState.IDLE) }
        }
    }

    fun onPhotoClicked(model: Any) {
        setState { copy(selectedPhotoModel = model) }
    }

    fun onDismissPhotoViewer() {
        setState { copy(selectedPhotoModel = null) }
    }

    fun deleteZone() {
        val zoneId = currentState.zone?.id ?: return

        launchWithResult(
            onStart = { copy(actionState = ZoneDetailsUiState.ActionState.DELETING) },
            onFinally = { copy(actionState = ZoneDetailsUiState.ActionState.IDLE) },
            block = { zoneRepository.deleteZone(zoneId) },
            onSuccess = {
                zoneRepository.invalidateZoneCache(zoneId)
                sendEvent(ZoneDetailsEvent.ShowSnackbar("Zone deleted successfully"))
                sendEvent(ZoneDetailsEvent.ZoneDeleted)
            },
            onError = { handleErrorWithMessage(getErrorMessage("Failed to delete zone", it)) },
        )
    }
}
