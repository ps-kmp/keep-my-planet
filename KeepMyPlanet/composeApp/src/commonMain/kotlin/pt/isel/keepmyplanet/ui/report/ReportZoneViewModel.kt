package pt.isel.keepmyplanet.ui.report

import pt.isel.keepmyplanet.data.api.ZoneApi
import pt.isel.keepmyplanet.domain.common.Description
import pt.isel.keepmyplanet.domain.zone.ZoneSeverity
import pt.isel.keepmyplanet.dto.zone.ReportZoneRequest
import pt.isel.keepmyplanet.ui.report.states.ReportZoneEvent
import pt.isel.keepmyplanet.ui.report.states.ReportZoneFormState
import pt.isel.keepmyplanet.ui.report.states.ReportZoneUiState
import pt.isel.keepmyplanet.ui.viewmodel.BaseViewModel

class ReportZoneViewModel(
    private val zoneApi: ZoneApi,
) : BaseViewModel<ReportZoneUiState>(ReportZoneUiState()) {
    override fun handleErrorWithMessage(message: String) {
        sendEvent(ReportZoneEvent.ShowSnackbar(message))
    }

    fun prepareReportForm(
        latitude: Double,
        longitude: Double,
    ) {
        setState { copy(form = ReportZoneFormState(latitude = latitude, longitude = longitude)) }
    }

    fun onReportDescriptionChange(description: String) {
        setState { copy(form = form.copy(description = description, descriptionError = null)) }
    }

    fun onReportSeverityChange(severity: ZoneSeverity) {
        setState { copy(form = form.copy(severity = severity)) }
    }

    fun submitZoneReport() {
        if (!validateForm() || !currentState.canSubmit) return

        val request =
            ReportZoneRequest(
                latitude = currentState.form.latitude,
                longitude = currentState.form.longitude,
                description = currentState.form.description,
                severity = currentState.form.severity.name,
                photoIds = emptySet(),
            )

        launchWithResult(
            onStart = { copy(actionState = ReportZoneUiState.ActionState.Submitting) },
            onFinally = { copy(actionState = ReportZoneUiState.ActionState.Idle) },
            block = { zoneApi.reportZone(request) },
            onSuccess = {
                sendEvent(ReportZoneEvent.ShowSnackbar("Zone reported successfully!"))
                sendEvent(ReportZoneEvent.ReportSuccessful)
            },
            onError = { handleErrorWithMessage(getErrorMessage("Failed to report zone", it)) },
        )
    }

    private fun validateForm(): Boolean {
        val descriptionError =
            try {
                Description(currentState.form.description)
                null
            } catch (e: IllegalArgumentException) {
                e.message
            }
        setState { copy(form = form.copy(descriptionError = descriptionError)) }
        return !currentState.form.hasError
    }
}
