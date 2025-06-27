package pt.isel.keepmyplanet.ui.zone.update

import pt.isel.keepmyplanet.data.api.ZoneApi
import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.domain.zone.ZoneSeverity
import pt.isel.keepmyplanet.dto.zone.UpdateZoneRequest
import pt.isel.keepmyplanet.mapper.zone.toZone
import pt.isel.keepmyplanet.ui.viewmodel.BaseViewModel
import pt.isel.keepmyplanet.ui.zone.update.states.UpdateZoneEvent
import pt.isel.keepmyplanet.ui.zone.update.states.UpdateZoneUiState

class UpdateZoneViewModel(
    private val zoneApi: ZoneApi,
) : BaseViewModel<UpdateZoneUiState>(UpdateZoneUiState()) {

    override fun handleErrorWithMessage(message: String) {
        sendEvent(UpdateZoneEvent.ShowSnackbar(message))
    }

    fun loadZone(zoneId: Id) {
        launchWithResult(
            onStart = { copy(isLoading = true, error = null) },
            onFinally = { copy(isLoading = false) },
            block = { zoneApi.getZoneDetails(zoneId.value) },
            onSuccess = { response ->
                val zone = response.toZone()
                setState {
                    copy(
                        zone = zone,
                        description = zone.description.value,
                        severity = zone.zoneSeverity
                    )
                }
            },
            onError = { setState { copy(error = getErrorMessage("Failed to load zone data", it)) } }
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
                val request = UpdateZoneRequest(
                    description = currentState.description,
                    severity = currentState.severity.name,
                    status = null
                )
                zoneApi.updateZone(zoneId.value, request)
            },
            onSuccess = {
                sendEvent(UpdateZoneEvent.ShowSnackbar("Zone updated successfully!"))
                sendEvent(UpdateZoneEvent.UpdateSuccessful)
            },
            onError = { handleErrorWithMessage(getErrorMessage("Failed to update zone", it)) }
        )
    }
}
