package pt.isel.keepmyplanet.ui.zone.update.states

import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.domain.zone.Zone
import pt.isel.keepmyplanet.domain.zone.ZoneSeverity
import pt.isel.keepmyplanet.ui.base.UiState

data class UpdateZoneUiState(
    val zone: Zone? = null,
    val description: String = "",
    val severity: ZoneSeverity = ZoneSeverity.UNKNOWN,
    val isLoading: Boolean = true,
    val isUpdating: Boolean = false,
    val error: String? = null,
    val descriptionError: String? = null,
    val photoModels: Map<Id, Any> = emptyMap(),
    val isUpdatingPhotos: Boolean = false,
) : UiState {
    val isFormEnabled: Boolean
        get() = !isLoading && !isUpdating && !isUpdatingPhotos

    val beforePhotos: List<Pair<Id, Any>>
        get() =
            zone?.beforePhotosIds?.mapNotNull { id -> photoModels[id]?.let { id to it } }
                ?: emptyList()

    val afterPhotos: List<Pair<Id, Any>>
        get() =
            zone?.afterPhotosIds?.mapNotNull { id -> photoModels[id]?.let { id to it } }
                ?: emptyList()
}
