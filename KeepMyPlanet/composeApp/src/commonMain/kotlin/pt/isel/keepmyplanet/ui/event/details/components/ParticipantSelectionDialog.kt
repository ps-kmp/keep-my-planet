package pt.isel.keepmyplanet.ui.event.details.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.AlertDialog
import androidx.compose.runtime.Composable
import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.domain.user.UserInfo
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ParticipantSelectionDialog(
    participants: List<UserInfo>,
    onParticipantSelected: (Id) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select New Organizer") },
        text = {
            if (participants.isEmpty()) {
                Text("There are no other participants to transfer ownership to.")
            } else {
                LazyColumn {
                    items(participants) { participant ->
                        Text(
                            text = participant.name.value,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onParticipantSelected(participant.id) }
                                .padding(vertical = 12.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
