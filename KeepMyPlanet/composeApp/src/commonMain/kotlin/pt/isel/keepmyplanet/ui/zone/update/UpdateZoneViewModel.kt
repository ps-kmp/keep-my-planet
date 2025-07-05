package pt.isel.keepmyplanet.ui.zone.update

import kotlinx.coroutines.launch
import pt.isel.keepmyplanet.data.repository.DefaultZoneRepository
import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.domain.zone.ZoneSeverity
import pt.isel.keepmyplanet.domain.zone.ZoneStatus
import pt.isel.keepmyplanet.dto.zone.UpdateZoneRequest
import pt.isel.keepmyplanet.ui.base.BaseViewModel
import pt.isel.keepmyplanet.ui.zone.update.states.UpdateZoneEvent
import pt.isel.keepmyplanet.ui.zone.update.states.UpdateZoneUiState

class UpdateZoneViewModel(
    private val zoneRepository: DefaultZoneRepository,
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
                if (zone.status == ZoneStatus.CLEANING_SCHEDULED) {
                    viewModelScope.launch {
                        zoneRepository
                            .revertToReported(zoneId)
                            .onFailure {
                                handleErrorWithMessage("Could not auto-revert zone status.")
                            }
                    }
                }
            },
            onError = {
                setState {
                    copy(
                        error = getErrorMessage("Failed to load zone data", it),
                    )
                }
            },
        )
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
}
