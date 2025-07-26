package pt.isel.keepmyplanet.ui.zone.update

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import pt.isel.keepmyplanet.data.repository.PhotoApiRepository
import pt.isel.keepmyplanet.data.repository.ZoneApiRepository
import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.domain.zone.ZoneSeverity
import pt.isel.keepmyplanet.domain.zone.ZoneStatus.CLEANED
import pt.isel.keepmyplanet.domain.zone.ZoneStatus.REPORTED
import pt.isel.keepmyplanet.dto.zone.UpdateZoneRequest
import pt.isel.keepmyplanet.ui.base.BaseViewModel
import pt.isel.keepmyplanet.ui.zone.update.states.UpdateZoneEvent
import pt.isel.keepmyplanet.ui.zone.update.states.UpdateZoneUiState

class UpdateZoneViewModel(
    private val zoneRepository: ZoneApiRepository,
    private val photoRepository: PhotoApiRepository,
) : BaseViewModel<UpdateZoneUiState>(UpdateZoneUiState()) {
    override fun handleErrorWithMessage(message: String) {
        sendEvent(UpdateZoneEvent.ShowSnackbar(message))
    }

    fun loadZone(zoneId: Id) {
        launchWithResult(
            onStart = { copy(isLoading = true, error = null) },
            onFinally = { copy(isLoading = false) },
            block = { zoneRepository.getZoneDetails(zoneId) },
            onSuccess = { zone ->
                setState {
                    copy(
                        zone = zone,
                        description = zone.description.value,
                        severity = zone.zoneSeverity,
                    )
                }
                fetchAndCachePhotos(zone.beforePhotosIds + zone.afterPhotosIds)
            },
            onError = {
                setState {
                    copy(error = getErrorMessage("Failed to load zone data", it))
                }
            },
        )
    }

    private fun fetchAndCachePhotos(photoIds: Set<Id>) {
        if (photoIds.isEmpty()) return
        viewModelScope.launch {
            val fetchedPhotoModels = currentState.photoModels.toMutableMap()
            val failures = mutableListOf<Throwable>()
            coroutineScope {
                photoIds
                    .map { photoId ->
                        async {
                            photoRepository
                                .getPhotoModel(photoId)
                                .onSuccess { model -> fetchedPhotoModels[photoId] = model }
                                .onFailure { failures.add(it) }
                        }
                    }.awaitAll()
            }
            if (failures.isNotEmpty()) {
                handleErrorWithMessage("Could not load some of the zone photos.")
            }
            setState { copy(photoModels = fetchedPhotoModels) }
        }
    }

    fun onDescriptionChange(newDescription: String) {
        setState { copy(description = newDescription, descriptionError = null) }
    }

    fun onSeverityChange(newSeverity: ZoneSeverity) {
        setState { copy(severity = newSeverity) }
    }

    fun submitUpdate() {
        val zoneId = currentState.zone?.id ?: return

        if (currentState.description.isBlank()) {
            setState { copy(descriptionError = "Description cannot be empty") }
            return
        }

        launchWithResult(
            onStart = { copy(isUpdating = true) },
            onFinally = { copy(isUpdating = false) },
            block = {
                val request =
                    UpdateZoneRequest(
                        description = currentState.description,
                        severity = currentState.severity.name,
                        status = null,
                    )
                zoneRepository.updateZone(zoneId, request)
            },
            onSuccess = {
                zoneRepository.invalidateZoneCache(zoneId)
                sendEvent(UpdateZoneEvent.ShowSnackbar("Zone updated successfully!"))
                sendEvent(UpdateZoneEvent.UpdateSuccessful)
            },
            onError = { handleErrorWithMessage(getErrorMessage("Failed to update zone", it)) },
        )
    }

    fun addPhoto(
        imageData: ByteArray,
        filename: String,
        type: String,
    ) {
        val zone = currentState.zone ?: return
        val zoneId = zone.id

        if (type.uppercase() == "BEFORE" && zone.status != REPORTED) {
            handleErrorWithMessage("Can only add 'Before' photos when zone status is REPORTED.")
            return
        }
        if (type.uppercase() == "AFTER" && zone.status != CLEANED) {
            handleErrorWithMessage("Can only add 'After' photos when zone status is CLEANED.")
            return
        }
        if ((type.uppercase() == "BEFORE" && currentState.beforePhotos.size >= 5) ||
            (type.uppercase() == "AFTER" && currentState.afterPhotos.size >= 5)
        ) {
            handleErrorWithMessage("You can only add up to 5 photos of each type.")
            return
        }

        launchWithResult(
            onStart = { copy(isUpdatingPhotos = true) },
            onFinally = { copy(isUpdatingPhotos = false) },
            block = {
                val photoResponse = photoRepository.createPhoto(imageData, filename).getOrThrow()
                zoneRepository.addPhotoToZone(zoneId, Id(photoResponse.id), type)
            },
            onSuccess = { updatedZone ->
                sendEvent(UpdateZoneEvent.ShowSnackbar("Photo added successfully!"))
                setState { copy(zone = updatedZone) }
                fetchAndCachePhotos(updatedZone.beforePhotosIds + updatedZone.afterPhotosIds)
            },
            onError = { handleErrorWithMessage(getErrorMessage("Failed to add photo", it)) },
        )
    }

    fun onRemovePhoto(photoId: Id) {
        val zoneId = currentState.zone?.id ?: return

        launchWithResult(
            onStart = { copy(isUpdatingPhotos = true) },
            onFinally = { copy(isUpdatingPhotos = false) },
            block = {
                zoneRepository.removePhotoFromZone(zoneId, photoId)
            },
            onSuccess = { updatedZone ->
                sendEvent(UpdateZoneEvent.ShowSnackbar("Photo removed successfully!"))
                setState {
                    copy(
                        zone = updatedZone,
                        photoModels = photoModels - photoId,
                    )
                }
            },
            onError = { handleErrorWithMessage(getErrorMessage("Failed to remove photo", it)) },
        )
    }
}
