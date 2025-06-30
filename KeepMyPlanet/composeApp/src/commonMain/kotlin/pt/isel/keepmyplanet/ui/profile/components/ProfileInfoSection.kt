package pt.isel.keepmyplanet.ui.profile.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import pt.isel.keepmyplanet.ui.components.FormField
import pt.isel.keepmyplanet.ui.components.LoadingButton
import pt.isel.keepmyplanet.ui.profile.states.UserProfileUiState

@Composable
fun ProfileInfoSection(
    uiState: UserProfileUiState,
    onNameChanged: (String) -> Unit,
    onEmailChanged: (String) -> Unit,
    onSaveProfileClicked: () -> Unit,
    onCancel: () -> Unit,
) {
    val isActionInProgress = uiState.actionState != UserProfileUiState.ActionState.IDLE

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        FormField(
            value = uiState.nameInput,
            onValueChange = onNameChanged,
            label = "Name",
            singleLine = true,
            enabled = !isActionInProgress,
            errorText = uiState.nameInputError,
        )

        FormField(
            value = uiState.emailInput,
            onValueChange = onEmailChanged,
            label = "Email",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true,
            enabled = !isActionInProgress,
            errorText = uiState.emailInputError,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(
                onClick = onCancel,
                enabled = !isActionInProgress,
            ) {
                Text("Cancel")
            }
            Spacer(Modifier.width(8.dp))
            LoadingButton(
                onClick = onSaveProfileClicked,
                enabled = uiState.isSaveProfileEnabled,
                isLoading = uiState.actionState == UserProfileUiState.ActionState.UPDATING_PROFILE,
                text = "Save Changes",
            )
        }
    }
}
