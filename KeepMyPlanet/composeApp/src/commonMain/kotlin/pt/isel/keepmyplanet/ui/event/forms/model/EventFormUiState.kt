package pt.isel.keepmyplanet.ui.event.forms.model

import pt.isel.keepmyplanet.domain.common.Id

data class EventFormUiState(
    val title: String = "",
    val description: String = "",
    val startDate: String = "",
    val maxParticipants: String = "",
    val zoneId: String = "",
    val isLoading: Boolean = false,
    val isEditMode: Boolean = false,
    val eventIdToEdit: Id? = null,
    val actionState: ActionState = ActionState.Idle,
    val titleError: String? = null,
    val descriptionError: String? = null,
    val startDateError: String? = null,
    val maxParticipantsError: String? = null,
    val zoneIdError: String? = null,
) {
    sealed interface ActionState {
        data object Idle : ActionState

        data object Submitting : ActionState
    }

    val hasErrors: Boolean
        get() =
            titleError != null ||
                descriptionError != null ||
                startDateError != null ||
                maxParticipantsError != null ||
                zoneIdError != null
}
