package pt.isel.keepmyplanet.ui.zone.report

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pt.isel.keepmyplanet.data.api.ZoneApi
import pt.isel.keepmyplanet.data.http.ApiException
import pt.isel.keepmyplanet.domain.common.Description
import pt.isel.keepmyplanet.domain.zone.ZoneSeverity
import pt.isel.keepmyplanet.dto.zone.ReportZoneRequest
import pt.isel.keepmyplanet.ui.zone.report.model.ReportZoneFormState
import pt.isel.keepmyplanet.ui.zone.report.model.ReportZoneScreenEvent
import pt.isel.keepmyplanet.ui.zone.report.model.ReportZoneUiState

class ReportZoneViewModel(
    private val zoneApi: ZoneApi,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ReportZoneUiState())
    val uiState = _uiState.asStateFlow()

    private val _events = Channel<ReportZoneScreenEvent>()
    val events = _events.receiveAsFlow()

    fun prepareReportForm(
        latitude: Double,
        longitude: Double,
    ) {
        _uiState.update {
            it.copy(form = ReportZoneFormState(latitude = latitude, longitude = longitude))
        }
    }

    fun onReportDescriptionChange(description: String) {
        _uiState.update {
            it.copy(
                form = it.form.copy(description = description, descriptionError = null),
            )
        }
    }

    fun onReportSeverityChange(severity: ZoneSeverity) {
        _uiState.update {
            it.copy(form = it.form.copy(severity = severity))
        }
    }

    fun submitZoneReport() {
        if (!validateForm()) return

        val formState = _uiState.value.form
        if (!formState.canSubmit) return

        viewModelScope.launch {
            _uiState.update { it.copy(form = it.form.copy(isSubmitting = true)) }

            val request =
                ReportZoneRequest(
                    latitude = formState.latitude,
                    longitude = formState.longitude,
                    description = formState.description,
                    severity = formState.severity.name,
                    photoIds = emptySet(),
                )

            zoneApi
                .reportZone(request)
                .onSuccess {
                    _events.send(ReportZoneScreenEvent.ShowSnackbar("Zone reported successfully!"))
                    _events.send(ReportZoneScreenEvent.ReportSuccessful)
                }.onFailure { error ->
                    handleError("Failed to report zone", error)
                }

            _uiState.update { it.copy(form = it.form.copy(isSubmitting = false)) }
        }
    }

    private fun validateForm(): Boolean {
        val formState = _uiState.value.form
        val descriptionError =
            try {
                Description(formState.description)
                null
            } catch (e: IllegalArgumentException) {
                e.message
            }

        _uiState.update {
            it.copy(form = it.form.copy(descriptionError = descriptionError))
        }

        return !uiState.value.form.hasError
    }

    private suspend fun handleError(
        prefix: String,
        error: Throwable,
    ) {
        val message =
            when (error) {
                is ApiException -> error.error.message
                else -> "$prefix: ${error.message ?: "Unknown error"}"
            }
        _events.send(ReportZoneScreenEvent.ShowSnackbar(message))
    }
}
