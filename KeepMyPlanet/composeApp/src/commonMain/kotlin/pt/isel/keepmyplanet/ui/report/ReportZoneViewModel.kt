package pt.isel.keepmyplanet.ui.report

import kotlinx.coroutines.launch
import pt.isel.keepmyplanet.data.cache.OfflineReportQueueRepository
import pt.isel.keepmyplanet.data.repository.DefaultPhotoRepository
import pt.isel.keepmyplanet.data.repository.DefaultZoneRepository
import pt.isel.keepmyplanet.data.service.ConnectivityService
import pt.isel.keepmyplanet.domain.common.Description
import pt.isel.keepmyplanet.domain.zone.ZoneSeverity
import pt.isel.keepmyplanet.dto.zone.ReportZoneRequest
import pt.isel.keepmyplanet.ui.base.BaseViewModel
import pt.isel.keepmyplanet.ui.report.states.ReportZoneEvent
import pt.isel.keepmyplanet.ui.report.states.ReportZoneUiState
import pt.isel.keepmyplanet.ui.report.states.SelectedImage

class ReportZoneViewModel(
    private val zoneRepository: DefaultZoneRepository,
    private val photoRepository: DefaultPhotoRepository,
    private val connectivityService: ConnectivityService,
    private val offlineReportQueueRepository: OfflineReportQueueRepository,
) : BaseViewModel<ReportZoneUiState>(ReportZoneUiState()) {
    override fun handleErrorWithMessage(message: String) {
        sendEvent(ReportZoneEvent.ShowSnackbar(message))
    }

    fun prepareReportForm(
        latitude: Double,
        longitude: Double,
    ) {
        setState { copy(latitude = latitude, longitude = longitude) }
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
            if (!connectivityService.isOnline.value) {
                queueReportForOfflineSubmission()
                return@launch
            }

            launchWithResult(
                onStart = { copy(actionState = ReportZoneUiState.ActionState.Submitting) },
                onFinally = { copy(actionState = ReportZoneUiState.ActionState.Idle) },
                block = {
                    val photoIds =
                        currentState.photos
                            .map {
                                photoRepository.createPhoto(it.data, it.filename).getOrThrow().id
                            }.toSet()

                    val request =
                        ReportZoneRequest(
                            latitude = currentState.latitude,
                            longitude = currentState.longitude,
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

    private suspend fun queueReportForOfflineSubmission() {
        val state = currentState
        offlineReportQueueRepository.queueReport(
            latitude = state.latitude,
            longitude = state.longitude,
            description = state.description,
            severity = state.severity,
            photos = state.photos,
        )
        sendEvent(ReportZoneEvent.ShowSnackbar("You're offline. Report queued for later."))
        sendEvent(ReportZoneEvent.ReportSuccessful)
    }

    private fun validateForm(): Boolean {
        val descriptionError =
            try {
                Description(currentState.description)
                null
            } catch (e: IllegalArgumentException) {
                e.message
            }
        setState { copy(descriptionError = descriptionError) }
        return !currentState.hasError
    }
}
