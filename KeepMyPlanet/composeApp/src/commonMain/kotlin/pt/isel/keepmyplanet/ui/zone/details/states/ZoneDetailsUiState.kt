package pt.isel.keepmyplanet.ui.zone.details.states

import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.domain.user.UserInfo
import pt.isel.keepmyplanet.domain.zone.Zone
import pt.isel.keepmyplanet.ui.base.UiState

data class ZoneDetailsUiState(
    val zone: Zone? = null,
    val reporter: UserInfo? = null,
    val isLoading: Boolean = false,
    val photoModels: Map<Id, Any> = emptyMap(),
    val error: String? = null,
    val canUserManageZone: Boolean = false,
    val actionState: ActionState = ActionState.IDLE,
    val selectedPhotoModel: Any? = null,
) : UiState {
    sealed interface ActionState {
        data object IDLE : ActionState

        data object ADDING_PHOTO : ActionState

        data object DELETING : ActionState
    }

    val beforePhotos: List<Pair<Id, Any>>
        get() =
            zone?.beforePhotosIds?.mapNotNull { id ->
                photoModels[id]?.let { id to it }
            } ?: emptyList()

    val afterPhotos: List<Pair<Id, Any>>
        get() =
            zone?.afterPhotosIds?.mapNotNull { id ->
                photoModels[id]?.let { id to it }
            } ?: emptyList()

    val isActionInProgress: Boolean
        get() = actionState != ActionState.IDLE
}
