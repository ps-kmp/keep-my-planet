@file:Suppress("ktlint:standard:function-naming")

package pt.isel.keepmyplanet.ui.user

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarDuration
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pt.isel.keepmyplanet.ui.components.AppTopBar
import pt.isel.keepmyplanet.ui.components.FullScreenLoading
import pt.isel.keepmyplanet.ui.user.components.DeleteAccountSection
import pt.isel.keepmyplanet.ui.user.components.PasswordChangeSection
import pt.isel.keepmyplanet.ui.user.components.ProfileInfoSection
import pt.isel.keepmyplanet.ui.user.model.UserProfileEvent
import pt.isel.keepmyplanet.ui.user.model.UserProfileUiState

@Composable
fun UserProfileScreen(
    viewModel: UserProfileViewModel,
    onNavigateToLogin: () -> Unit = {},
    onNavigateBack: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is UserProfileEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(
                        message = event.message,
                        duration = SnackbarDuration.Short,
                    )
                }

                is UserProfileEvent.NavigateToLogin -> onNavigateToLogin()
            }
        }
    }

    UserProfileScreenContent(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onNavigateBack = onNavigateBack,
        onNameChanged = viewModel::onNameChanged,
        onEmailChanged = viewModel::onEmailChanged,
        onOldPasswordChanged = viewModel::onOldPasswordChanged,
        onNewPasswordChanged = viewModel::onNewPasswordChanged,
        onConfirmPasswordChanged = viewModel::onConfirmPasswordChanged,
        onEditProfileToggled = viewModel::onEditProfileToggled,
        onSaveProfileClicked = viewModel::onSaveProfileClicked,
        onPasswordChangeToggled = viewModel::onPasswordChangeToggled,
        onChangePasswordClicked = viewModel::onChangePasswordClicked,
        onConfirmDeleteAccount = viewModel::onDeleteAccountClicked,
    )
}

@Composable
fun UserProfileScreenContent(
    uiState: UserProfileUiState,
    snackbarHostState: SnackbarHostState,
    onNavigateBack: () -> Unit,
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
) {
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = { AppTopBar(title = "User Profile", onNavigateBack = onNavigateBack) },
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (uiState.isLoading) {
                FullScreenLoading()
            } else if (uiState.userDetails == null) {
                Text(
                    text = "Failed to load user profile.",
                    modifier = Modifier.align(Alignment.Center).padding(16.dp),
                )
            } else {
                Column(
                    modifier =
                        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profile Picture",
                        modifier = Modifier.size(100.dp),
                    )

                    ProfileInfoSection(
                        uiState = uiState,
                        onNameChanged = onNameChanged,
                        onEmailChanged = onEmailChanged,
                        onEditProfileToggled = onEditProfileToggled,
                        onSaveProfileClicked = onSaveProfileClicked,
                    )

                    Divider()

                    PasswordChangeSection(
                        uiState = uiState,
                        onOldPasswordChanged = onOldPasswordChanged,
                        onNewPasswordChanged = onNewPasswordChanged,
                        onConfirmPasswordChanged = onConfirmPasswordChanged,
                        onChangePasswordClicked = onChangePasswordClicked,
                    )
                    Button(
                        onClick = onPasswordChangeToggled,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            if (uiState.showPasswordChangeSection) {
                                "Cancel Password Change"
                            } else {
                                "Change Password"
                            },
                        )
                    }

                    DeleteAccountSection(uiState, onConfirmDeleteAccount)
                }
            }
        }
    }
}
