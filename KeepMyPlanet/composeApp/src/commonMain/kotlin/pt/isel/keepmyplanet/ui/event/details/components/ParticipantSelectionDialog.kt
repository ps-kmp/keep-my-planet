package pt.isel.keepmyplanet.ui.event.details.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.domain.user.UserInfo
import pt.isel.keepmyplanet.ui.components.InfoRow

@Composable
fun ParticipantSelectionDialog(
    participants: List<UserInfo>,
    onParticipantSelected: (Id) -> Unit,
    onDismiss: () -> Unit,
) {
    var selectedParticipantForDetails by remember { mutableStateOf<UserInfo?>(null) }

    if (selectedParticipantForDetails != null) {
        val user = selectedParticipantForDetails!!
        AlertDialog(
            onDismissRequest = { selectedParticipantForDetails = null },
            title = { Text(user.name.value) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    InfoRow(icon = Icons.Default.Person, text = "Name: ${user.name.value}")
                }
            },
            confirmButton = {
                Button(onClick = {
                    onParticipantSelected(user.id)
                    selectedParticipantForDetails = null
                }) {
                    Text("Select as Organizer")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedParticipantForDetails = null }) {
                    Text("Back")
                }
            },
        )
    }

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
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedParticipantForDetails = participant }
                                    .padding(vertical = 12.dp),
                        )
                        HorizontalDivider()
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}
