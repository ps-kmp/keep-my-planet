package pt.isel.keepmyplanet.ui.event.forms.model

data class EventFormUiState(
    val title: String = "",
    val description: String = "",
    val startDate: String = "",
    val maxParticipants: String = "",
    val zoneId: String = "",
    val isLoading: Boolean = false,
    val isEditMode: Boolean = false,
    val isSubmitting: Boolean = false,
    val titleError: String? = null,
    val descriptionError: String? = null,
    val startDateError: String? = null,
    val maxParticipantsError: String? = null,
    val zoneIdError: String? = null,
) {
    val hasErrors: Boolean
        get() =
            titleError != null ||
                descriptionError != null ||
                startDateError != null ||
                maxParticipantsError != null ||
                zoneIdError != null
}
