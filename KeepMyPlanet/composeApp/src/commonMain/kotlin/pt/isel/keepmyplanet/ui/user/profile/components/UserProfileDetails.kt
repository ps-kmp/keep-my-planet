package pt.isel.keepmyplanet.ui.user.profile.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pt.isel.keepmyplanet.ui.components.DetailCard
import pt.isel.keepmyplanet.ui.components.InfoRow
import pt.isel.keepmyplanet.ui.user.profile.model.UserProfileUiState

@Composable
fun UserProfileDetails(
    uiState: UserProfileUiState,
    onNameChanged: (String) -> Unit,
    onEmailChanged: (String) -> Unit,
    onOldPasswordChanged: (String) -> Unit,
    onNewPasswordChanged: (String) -> Unit,
    onConfirmPasswordChanged: (String) -> Unit,
    onEditProfileToggled: () -> Unit,
    onSaveProfileClicked: () -> Unit,
    onPasswordChangeToggled: () -> Unit,
    onChangePasswordClicked: () -> Unit,
    onConfirmDeleteAccount: () -> Unit,
    onNavigateToStats: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        ProfileHeader(user = uiState.userDetails!!)

        DetailCard(
            title = "Personal Information",
            actions = {
                if (!uiState.isEditingProfile) {
                    IconButton(onClick = onEditProfileToggled) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Profile")
                    }
                }
            },
        ) {
            AnimatedVisibility(visible = uiState.isEditingProfile) {
                ProfileInfoSection(
                    uiState = uiState,
                    onNameChanged = onNameChanged,
                    onEmailChanged = onEmailChanged,
                    onSaveProfileClicked = onSaveProfileClicked,
                    onCancel = onEditProfileToggled,
                )
            }
            AnimatedVisibility(visible = !uiState.isEditingProfile) {
                ProfileInfoDisplay(user = uiState.userDetails)
            }
        }

        DetailCard(title = "My Activity") {
            InfoRow(
                icon = Icons.AutoMirrored.Filled.TrendingUp,
                text = "View your contribution and attended events.",
            )
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = onNavigateToStats,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("View My Stats")
            }
        }

        DetailCard(title = "Security") {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Button(
                    onClick = onPasswordChangeToggled,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        if (uiState.showPasswordChangeSection) "Cancel" else "Change Password",
                    )
                }
                PasswordChangeSection(
                    uiState = uiState,
                    onOldPasswordChanged = onOldPasswordChanged,
                    onNewPasswordChanged = onNewPasswordChanged,
                    onConfirmPasswordChanged = onConfirmPasswordChanged,
                    onChangePasswordClicked = onChangePasswordClicked,
                )
                Divider()
                DeleteAccountSection(uiState, onConfirmDeleteAccount)
            }
        }
    }
}
