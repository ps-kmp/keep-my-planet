package pt.isel.keepmyplanet.ui.event.model

data class EventFormUiState(
    val title: String = "",
    val description: String = "",
    val startDate: String = "",
    val maxParticipants: String = "",
    val zoneId: String = "",
    val isSubmitting: Boolean = false,
) {
    val canSubmit: Boolean
        get() =
            title.isNotBlank() &&
                description.isNotBlank() &&
                startDate.isNotBlank() &&
                zoneId.toUIntOrNull() != null &&
                !isSubmitting
}
