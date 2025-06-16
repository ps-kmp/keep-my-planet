package pt.isel.keepmyplanet.ui.zone

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
import pt.isel.keepmyplanet.mapper.zone.toZone
import pt.isel.keepmyplanet.ui.zone.model.ReportZoneFormState
import pt.isel.keepmyplanet.ui.zone.model.ZoneScreenEvent
import pt.isel.keepmyplanet.ui.zone.model.ZoneUiState

class ZoneViewModel(
    private val zoneApi: ZoneApi,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ZoneUiState())
    val uiState = _uiState.asStateFlow()

    private val _events = Channel<ZoneScreenEvent>()
    val events = _events.receiveAsFlow()

    fun loadZoneDetails(zoneId: UInt) {
        _uiState.update { it.copy(isLoading = true, zoneDetails = null) }
        viewModelScope.launch {
            zoneApi
                .getZoneDetails(zoneId)
                .onSuccess { response ->
                    _uiState.update { it.copy(isLoading = false, zoneDetails = response.toZone()) }
                }.onFailure { error ->
                    _uiState.update { it.copy(isLoading = false) }
                    handleError("Failed to load zone", error)
                }
        }
    }

    fun prepareReportForm(
        latitude: Double,
        longitude: Double,
    ) {
        _uiState.update {
            it.copy(reportForm = ReportZoneFormState(latitude = latitude, longitude = longitude))
        }
    }

    fun onReportDescriptionChange(description: String) {
        _uiState.update {
            it.copy(
                reportForm = it.reportForm.copy(description = description, descriptionError = null),
            )
        }
    }

    fun onReportSeverityChange(severity: ZoneSeverity) {
        _uiState.update {
            it.copy(reportForm = it.reportForm.copy(severity = severity))
        }
    }

    fun submitZoneReport() {
        if (!validateForm()) return

        val formState = _uiState.value.reportForm
        if (!formState.canSubmit) return

        viewModelScope.launch {
            _uiState.update { it.copy(reportForm = it.reportForm.copy(isSubmitting = true)) }

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
                    _events.send(ZoneScreenEvent.ShowSnackbar("Zone reported successfully!"))
                    _events.send(ZoneScreenEvent.ReportSuccessful)
                }.onFailure { error ->
                    handleError("Failed to report zone", error)
                }

            _uiState.update { it.copy(reportForm = it.reportForm.copy(isSubmitting = false)) }
        }
    }

    private fun validateForm(): Boolean {
        val formState = _uiState.value.reportForm
        val descriptionError =
            try {
                Description(formState.description)
                null
            } catch (e: IllegalArgumentException) {
                e.message
            }

        _uiState.update {
            it.copy(reportForm = it.reportForm.copy(descriptionError = descriptionError))
        }

        return !_uiState.value.reportForm.hasError
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
        _events.send(ZoneScreenEvent.ShowSnackbar(message))
    }
}
