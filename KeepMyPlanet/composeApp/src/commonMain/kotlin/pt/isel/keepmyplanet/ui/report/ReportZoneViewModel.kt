package pt.isel.keepmyplanet.ui.report

import kotlinx.coroutines.launch
import pt.isel.keepmyplanet.data.repository.PhotoApiRepository
import pt.isel.keepmyplanet.data.repository.ZoneApiRepository
import pt.isel.keepmyplanet.domain.common.Description
import pt.isel.keepmyplanet.domain.zone.Radius
import pt.isel.keepmyplanet.domain.zone.ZoneSeverity
import pt.isel.keepmyplanet.dto.zone.ReportZoneRequest
import pt.isel.keepmyplanet.ui.base.BaseViewModel
import pt.isel.keepmyplanet.ui.report.states.ReportZoneEvent
import pt.isel.keepmyplanet.ui.report.states.ReportZoneUiState
import pt.isel.keepmyplanet.ui.report.states.SelectedImage

class ReportZoneViewModel(
    private val zoneRepository: ZoneApiRepository,
    private val photoRepository: PhotoApiRepository,
) : BaseViewModel<ReportZoneUiState>(ReportZoneUiState()) {
    override fun handleErrorWithMessage(message: String) {
        sendEvent(ReportZoneEvent.ShowSnackbar(message))
    }

    fun prepareReportForm(
        latitude: Double,
        longitude: Double,
        radius: Double,
    ) {
        setState { copy(latitude = latitude, longitude = longitude, radius = radius) }
    }

    fun onReportDescriptionChange(description: String) {
        setState { copy(description = description, descriptionError = null) }
    }

    fun onReportSeverityChange(severity: ZoneSeverity) {
        setState { copy(severity = severity) }
    }

    fun onPhotoSelected(
        imageData: ByteArray,
        filename: String,
    ) {
        if (currentState.photos.size >= 5) {
            sendEvent(ReportZoneEvent.ShowSnackbar("You can only add up to 5 photos."))
            return
        }
        setState { copy(photos = photos + SelectedImage(imageData, filename)) }
    }

    fun onRemovePhoto(image: SelectedImage) {
        setState { copy(photos = photos - image) }
    }

    fun submitZoneReport() {
        if (!validateForm() || !currentState.canSubmit) return

        viewModelScope.launch {
            launchWithResult(
                onStart = { copy(actionState = ReportZoneUiState.ActionState.Submitting) },
                onFinally = { copy(actionState = ReportZoneUiState.ActionState.Idle) },
                block = {
                    val photoIds = mutableSetOf<UInt>()
                    for (photo in currentState.photos) {
                        val result = photoRepository.createPhoto(photo.data, photo.filename)
                        if (result.isFailure) {
                            return@launchWithResult Result.failure(result.exceptionOrNull()!!)
                        }
                        photoIds.add(result.getOrThrow().id)
                    }

                    val request =
                        ReportZoneRequest(
                            latitude = currentState.latitude,
                            longitude = currentState.longitude,
                            radius = currentState.radius,
                            description = currentState.description,
                            severity = currentState.severity.name,
                            photoIds = photoIds,
                        )
                    zoneRepository.reportZone(request)
                },
                onSuccess = {
                    sendEvent(ReportZoneEvent.ShowSnackbar("Zone reported successfully!"))
                    sendEvent(ReportZoneEvent.ReportSuccessful)
                },
                onError = { handleErrorWithMessage(getErrorMessage("Failed to report zone", it)) },
            )
        }
    }

    private fun validateForm(): Boolean {
        val descriptionError =
            try {
                Description(currentState.description)
                null
            } catch (e: IllegalArgumentException) {
                e.message
            }
        Radius(currentState.radius)
        setState { copy(descriptionError = descriptionError) }
        return !currentState.hasError
    }
}
